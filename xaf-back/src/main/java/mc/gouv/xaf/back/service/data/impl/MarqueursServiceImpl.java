package mc.gouv.xaf.back.service.data.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import mc.gouv.xaf.back.data.dao.MarqueursRepository;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.entity.MarqueurBO;
import mc.gouv.xaf.back.data.transformer.MarqueursTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.MarqueursService;
import mc.gouv.xaf.shared.dto.MarqueurDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
public class MarqueursServiceImpl implements MarqueursService {

    @Autowired
    private MarqueursRepository marqueursRepository;

    @Autowired
    private MarqueursTransformer marqueursTransformer;

    @Autowired
    private DemandesConfigService demandesConfigService;

    @Override
    public List<MarqueurDTO> getMarqueurs(String buildId) {
        List<MarqueurBO> marqueurBOS = marqueursRepository.findAllByBuildId(buildId);
        return marqueursTransformer.bos2Dtos(marqueurBOS);
    }

    @Override
    public MarqueurDTO saveOrUpdateMarqueur(MarqueurDTO marqueurDTO) {
        // on calcule le type
        DemandeConfigBO config = demandesConfigService.getConfig(marqueurDTO.getBuildId());
        setDescriptionTypeOptions(marqueurDTO, config.getContenu());
        // Création
        if (marqueurDTO.getPkMarqueur() == null) {
            MarqueurBO bo = marqueursTransformer.dto2Bo(marqueurDTO);
            bo = marqueursRepository.save(bo);
            return marqueursTransformer.bo2Dto(bo);
        }
        // Mise à jour
        else {
            Optional<MarqueurBO> marqueurBOOpt = marqueursRepository.findById(marqueurDTO.getPkMarqueur());
            if (marqueurBOOpt.isEmpty()) {
                throw new DemarchesServiceException("Le marqueur spécifié est introuvable", HttpStatus.NOT_FOUND);
            }

            MarqueurBO marqueurBO = marqueurBOOpt.get();
            marqueurBO.setDescription(marqueurDTO.getDescription());
            marqueurBO.setIdentifiant(marqueurDTO.getIdentifiant());
            marqueurBO.setChemin(marqueurDTO.getChemin());
            marqueurBO.setBuildId(marqueurDTO.getBuildId());
            marqueurBO.setType(marqueurDTO.getType());
            marqueurBO.setOptions(marqueurDTO.getOptions());
            marqueurBO = marqueursRepository.save(marqueurBO);

            return marqueursTransformer.bo2Dto(marqueurBO);

        }
    }

    @Override
    public void deleteMarqueur(Integer pkMarqueur) {
        Optional<MarqueurBO> marqueurBO = marqueursRepository.findById(pkMarqueur);
        if (marqueurBO.isEmpty()) {
            throw new DemarchesServiceException("Le marqueur spécifié est introuvable", HttpStatus.NOT_FOUND);
        }
        marqueursRepository.delete(marqueurBO.get());
    }

    @Override
    public void copyOrGenerateMarqueurs(String lastBuildId, String buildId, List<String> modelPaths, JsonNode config) {
        List<MarqueurDTO> marqueurDTOS = getMarqueurs(lastBuildId);
        // on copie les marqueurs du précédent build id s'il y en a
        if (!marqueurDTOS.isEmpty()) {
            for (MarqueurDTO marqueurDTO : marqueurDTOS) {
                marqueurDTO.setPkMarqueur(null);
                marqueurDTO.setBuildId(buildId);
                // vérifier si le chemin existe toujours dans le nouveau config
                if (marqueurDTO.getChemin() != null && !modelPaths.contains(marqueurDTO.getChemin())) {
                    marqueurDTO.setChemin(null);
                }
            }
        }
        // on génère tous les autres
        setMarqueursFromModelPaths(modelPaths, marqueurDTOS, buildId, config);

        marqueursRepository.saveAll(marqueursTransformer.dtos2Bos(marqueurDTOS));
    }

    @Override
    public void resetMarqueurs() {
        marqueursRepository.deleteAll();
        List<DemandeConfigBO> configs = demandesConfigService.getConfigsBO();
        for (DemandeConfigBO config : configs) {
            List<MarqueurDTO> marqueurDTOS = new ArrayList<>();

            setMarqueursFromModelPaths(
                    demandesConfigService.getModelPaths(config.getContenu().get("modelPaths").get("marqueurs")),
                    marqueurDTOS, config.getBuildId(), config.getContenu());
            marqueursRepository.saveAll(marqueursTransformer.dtos2Bos(marqueurDTOS));
        }
    }

    private void setMarqueursFromModelPaths(List<String> modelPaths, List<MarqueurDTO> marqueurDTOS, String buildId,
            JsonNode config) {
        for (String modelPath : modelPaths) {
            String id = pathToCamelCase(modelPath);
            // si le marqueur est déjà présent (du précédent buildId par exemple), on ne génère pas le marqueur
            if (marqueurDTOS.stream().noneMatch(marqueurDTO -> id.equals(marqueurDTO.getIdentifiant()))) {
                MarqueurDTO marqueur = new MarqueurDTO();
                marqueur.setChemin(modelPath);
                marqueur.setIdentifiant(id);
                marqueur.setBuildId(buildId);
                setDescriptionTypeOptions(marqueur, config);
                marqueurDTOS.add(marqueur);
            }
        }
    }

    private MarqueurDTO setDescriptionTypeOptions(MarqueurDTO marqueurDTO, JsonNode config) {
        String modelPath = marqueurDTO.getChemin();
        JsonNode sections = config.get("recap").get("sections");
        JsonNode mappings = config.get("mappings");
        String modifiedModelPath = modelPath;
        String suffixeFound = null;
        String[] possibleSuffixesToRemove = { "ligne1", "ligne2", "ligne3", "ville", "pays", "codePostal", "bic",
                "iban", "titulaire", "indicatif", "numero" };
        for (String suffix : possibleSuffixesToRemove) {
            String suffixDot = "." + suffix;
            if (modelPath.endsWith(suffixDot)) {
                suffixeFound = suffix;
                modifiedModelPath = modelPath.substring(0, modelPath.length() - suffixDot.length());
                break;
            }
        }
        // récupérer tous les champs, qu'ils soient dans des sections ou des sous sections
        List<JsonNode> champsNodes = sections.findValues("champs");
        for (JsonNode champs : champsNodes) {
            for (JsonNode champ : champs) {
                if (champ.get("path").asText().equals(modifiedModelPath) && !champ.get("type").asText()
                        .equals("tableau")) {
                    if (marqueurDTO.getDescription() == null || marqueurDTO.getDescription().isEmpty()) {
                        // si c'est un type particulier on ajoute le suffixe
                        String description = champ.get("label").asText();
                        if (suffixeFound != null) {
                            description = description + " - " + suffixeFound;
                        }
                        marqueurDTO.setDescription(description);
                    }
                    marqueurDTO.setType(champ.get("type").asText());
                    setMarqueursOptions(marqueurDTO, champ, mappings);
                    return marqueurDTO;
                }
            }
        }
        // si on a rien, on va chercher du côté des tableaux
        List<Map.Entry<String, JsonNode>> tableauWithTitle = new ArrayList<>();
        extractTableauNodesWithParents(sections, null, tableauWithTitle);
        for (Map.Entry<String, JsonNode> entry : tableauWithTitle) {
            String title = "Tableau " + entry.getKey();
            JsonNode tableau = entry.getValue();
            // on regarde d'abord si c'est le chemin racine du tableau
            if (tableau.get("path").asText().equals(modifiedModelPath)) {
                if (marqueurDTO.getDescription() == null || marqueurDTO.getDescription().isEmpty()) {
                    marqueurDTO.setDescription(title);
                }
                marqueurDTO.setType(tableau.get("type").asText());
                return marqueurDTO;
            }
            for (JsonNode column : tableau.get("columns")) {
                String path = column.get("path").asText();
                if (path.equals(modifiedModelPath.substring(modifiedModelPath.lastIndexOf('.') + 1))) {
                    if (marqueurDTO.getDescription() == null || marqueurDTO.getDescription().isEmpty()) {
                        // si c'est un type particulier on ajoute le suffixe
                        String description = column.get("label").asText();
                        if (suffixeFound != null) {
                            description = description + " - " + suffixeFound;
                        }
                        marqueurDTO.setDescription(title + " - " + description);
                    }
                    marqueurDTO.setType(column.get("type").asText());
                    setMarqueursOptions(marqueurDTO, column, mappings);
                    return marqueurDTO;
                }
            }

        }
        return marqueurDTO;
    }

    private void setMarqueursOptions(MarqueurDTO marqueurDTO, JsonNode champ, JsonNode mappings) {
        // si choix ou choixMultiple on sauvegarde les valeurs possibles dans options
        JsonNode isDynamic = champ.get("isDynamic");
        if (("choix".equals(marqueurDTO.getType()) || "choixMultiple".equals(marqueurDTO.getType())) && (
                isDynamic == null || !isDynamic.asBoolean())) {
            marqueurDTO.setOptions(
                    mappings.get(champ.get("mapping").asText()).get("languages").get("fr").get("values"));
        }
    }

    private void extractTableauNodesWithParents(JsonNode node, String parentTitle,
            List<Map.Entry<String, JsonNode>> tableauWithTitle) {
        if (node.isObject()) {
            JsonNode typeNode = node.get("type");
            if (typeNode != null && "tableau".equals(typeNode.asText())) {
                // On suppose que le titre est dans le nœud parent direct
                tableauWithTitle.add(Map.entry(parentTitle, node));
            }

            JsonNode titreNode = node.get("titre");
            String currentTitle = (titreNode != null) ? titreNode.asText() : parentTitle;

            // Parcourir les enfants de l'objet
            node.fields().forEachRemaining(
                    entry -> extractTableauNodesWithParents(entry.getValue(), currentTitle, tableauWithTitle));
        } else if (node.isArray()) {
            // Si le nœud est un tableau
            for (int i = 0; i < node.size(); i++) {
                extractTableauNodesWithParents(node.get(i), parentTitle, tableauWithTitle);
            }
        }
    }

    private String pathToCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Diviser la chaîne en segments
        String[] parts = input.split("\\.");

        // Si la chaîne contient moins de deux parties, rien à transformer
        if (parts.length < 2) {
            return input;
        }

        StringBuilder result = new StringBuilder(parts[1]);

        for (int i = 2; i < parts.length; i++) {
            String part = parts[i];
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }

        return result.toString();
    }

    @Override
    public String exportConfig() throws IOException {
        Iterable<MarqueurBO> marqueurs = marqueursRepository.findAll();
        List<MarqueurDTO> list = new ArrayList<>();
        for (MarqueurBO marqueur : marqueurs) {
            list.add(marqueursTransformer.bo2Dto(marqueur));
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);
    }

    @Override
    public void importConfig(byte[] file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        List<MarqueurDTO> marqueurList = mapper.readValue(file, new TypeReference<>() {

        });
        if (marqueurList != null) {
            marqueursRepository.deleteAll();
            marqueursRepository.saveAll(marqueursTransformer.dtos2Bos(marqueurList));
        }

    }

    @Override
    public JsonNode buildDemande(Map<String, String> donnees, List<Map<String, String>> donneesTableaux) {
        // Configuration initiale
        String lastBuildId = demandesConfigService.getLastBuildId();
        List<MarqueurDTO> marqueurs = getMarqueurs(lastBuildId);

        // Utilisation de ObjectMapper pour créer un ObjectNode
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode demandeNode = mapper.createObjectNode();

        // Iteration sur chaque entrée
        donnees.forEach((marqueur, valeur) -> {
            MarqueurDTO marqueurMatch = marqueurs.stream().filter(m -> m.getIdentifiant().equals(marqueur)).findFirst()
                    .orElse(null);

            if (marqueurMatch != null) {
                String chemin = marqueurMatch.getChemin();
                chemin = chemin.replaceFirst("^contenu\\.", "");
                String[] segments = chemin.split("\\.");

                // Permet d'itérer et de configurer chaque niveau du chemin
                ObjectNode currentNode = demandeNode;
                for (int i = 0; i < segments.length; i++) {
                    String segment = segments[i];

                    // Si c'est le dernier segment, ajouter la valeur
                    if (i == segments.length - 1) {
                        currentNode.put(segment, valeur);
                    } else {
                        // Si le segment n'existe pas, créer une nouvelle branche
                        if (!currentNode.has(segment)) {
                            currentNode.set(segment, mapper.createObjectNode());
                        }
                        // Descendre d'un niveau dans le noeud courant
                        currentNode = (ObjectNode) currentNode.get(segment);
                    }
                }
            }
        });

        if (donneesTableaux != null) {
            // Traitement des tableaux
            donneesTableaux.forEach(tableau -> {
                // Assume que tous les objets du tableau partagent le même chemin initial commun
                Optional<String> cheminCommumOptionnel = tableau.keySet().stream()
                        .map(marqueur -> marqueurs.stream().filter(m -> m.getIdentifiant().equals(marqueur)).findFirst()
                                .map(MarqueurDTO::getChemin).orElse(null)).filter(Objects::nonNull).findFirst();

                if (cheminCommumOptionnel.isPresent()) {
                    String cheminCommum = cheminCommumOptionnel.get();
                    // Suppression de "contenu"
                    cheminCommum = cheminCommum.replaceFirst("^contenu\\.", "");
                    String[] segments = cheminCommum.split("\\.");

                    ObjectNode currentNode = demandeNode;
                    ArrayNode arrayNode;

                    for (int i = 0; i < segments.length - 2; i++) { // Traverse jusqu'à deux segments avant la fin
                        String segment = segments[i];

                        if (!currentNode.has(segment)) {
                            currentNode.set(segment, mapper.createObjectNode());
                        }
                        currentNode = (ObjectNode) currentNode.get(segment);
                    }

                    String segmentFinal = segments[segments.length - 2]; // fin correcte du chemin
                    if (!currentNode.has(segmentFinal)) {
                        arrayNode = mapper.createArrayNode();
                        currentNode.set(segmentFinal, arrayNode);
                    } else {
                        arrayNode = (ArrayNode) currentNode.get(segmentFinal);
                    }

                    ObjectNode itemNode = mapper.createObjectNode();
                    for (Map.Entry<String, String> entry : tableau.entrySet()) {
                        String key = entry.getKey();
                        MarqueurDTO marqueur = marqueurs.stream().filter(m -> m.getIdentifiant().equals(key))
                                .findFirst().orElse(null);
                        if (marqueur != null) {
                            String path = marqueur.getChemin();
                            String[] elements = path.split("\\.");
                            String lastPath = elements[elements.length - 1];
                            String value = entry.getValue();
                            String type = marqueur.getType();
                            if (type.equals("adresse") || type.equals("adresseMC") || type.equals(
                                    "telephone") || type.equals("iban")) {
                                // cas des types spéciaux
                                // récupérer l'avant-dernier élément du tableau
                                String rootPath = elements[elements.length - 2];
                                // vérifier si le noeud existe déjà
                                JsonNode rootNode = itemNode.get(rootPath);
                                if (rootNode != null && !rootNode.isMissingNode()) {
                                    ((ObjectNode) rootNode).put(lastPath, value);
                                } else {
                                    ObjectNode newNode = mapper.createObjectNode();
                                    newNode.put(lastPath, value);
                                    itemNode.put(rootPath, newNode);
                                }
                            } else {
                                // cas type simple
                                itemNode.put(lastPath, value);
                            }

                        }
                    }
                    arrayNode.add(itemNode);
                }
            });
        }

        return demandeNode;
    }

    @Override
    public JsonNode buildDemande(Map<String, String> donnees) {
        return buildDemande(donnees, null);
    }

}
