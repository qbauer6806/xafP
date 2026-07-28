package mc.gouv.xaf.back.service.data.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.MarqueursRepository;
import mc.gouv.xaf.back.data.entity.MarqueurBO;
import mc.gouv.xaf.back.data.transformer.MarqueursTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.MarqueursService;
import mc.gouv.xaf.shared.dto.BuildDemandeFromMarqueursDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.MappingOptionDTO;
import mc.gouv.xaf.shared.dto.MarqueurDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MarqueursServiceImpl implements MarqueursService {

    private final MarqueursRepository marqueursRepository;
    private final MarqueursTransformer marqueursTransformer;
    private final DemandesConfigHelperService demandesConfigHelperService;

    @Override
    public List<MarqueurDTO> getMarqueurs(String buildId) {
        List<MarqueurBO> marqueurBOS = marqueursRepository.findAllByBuildId(buildId);
        return marqueursTransformer.bos2Dtos(marqueurBOS);
    }

    @Override
    public MarqueurDTO getMarqueur(String buildId, String marqueurId) {
        List<MarqueurBO> marqueurBO = marqueursRepository.findAllByBuildIdAndIdentifiant(buildId, marqueurId);
        if (!marqueurBO.isEmpty()) {
            return marqueursTransformer.bo2Dto(marqueurBO.getFirst());
        }
        return null;
    }

    @Override
    public MarqueurDTO saveOrUpdateMarqueur(MarqueurDTO marqueurDTO, JsonNode configContenu) {
        // on calcule le type
        setDescriptionTypeOptions(marqueurDTO, configContenu);
        // Création
        if (marqueurDTO.getPkMarqueur() == null) {
            MarqueurBO bo = marqueursTransformer.dto2Bo(marqueurDTO);
            bo = marqueursRepository.save(bo);
            return marqueursTransformer.bo2Dto(bo);
        }
        // Mise à jour
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

    private void setMarqueursFromModelPaths(List<String> modelPaths, List<MarqueurDTO> marqueurDTOS, String buildId,
            JsonNode config) {
        for (String modelPath : modelPaths) {
            String id = pathToCamelCase(modelPath);

            // Rechercher si le marqueur existe déjà
            Optional<MarqueurDTO> existing = marqueurDTOS.stream().filter(m -> id.equals(m.getIdentifiant()))
                    .findFirst();

            if (existing.isPresent()) {
                // ➜ Mettre à jour uniquement descriptionTypeOptions
                setDescriptionTypeOptions(existing.get(), config);
            } else {
                // ➜ Créer un nouveau marqueur
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
                if (champ.get("path").asString().equals(modifiedModelPath) && !champ.get("type").asString()
                        .equals("tableau")) {
                    if (marqueurDTO.getDescription() == null || marqueurDTO.getDescription().isEmpty()) {
                        // si c'est un type particulier on ajoute le suffixe
                        String description = champ.get("label").asString();
                        if (suffixeFound != null) {
                            description = description + " - " + suffixeFound;
                        }
                        marqueurDTO.setDescription(description);
                    }
                    marqueurDTO.setType(champ.get("type").asString());
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
            if (tableau.get("path").asString().equals(modifiedModelPath)) {
                if (marqueurDTO.getDescription() == null || marqueurDTO.getDescription().isEmpty()) {
                    marqueurDTO.setDescription(title);
                }
                marqueurDTO.setType(tableau.get("type").asString());
                return marqueurDTO;
            }
            for (JsonNode column : tableau.get("columns")) {
                String path = column.get("path").asString();
                if (path.equals(modifiedModelPath.substring(modifiedModelPath.lastIndexOf('.') + 1))) {
                    if (marqueurDTO.getDescription() == null || marqueurDTO.getDescription().isEmpty()) {
                        // si c'est un type particulier on ajoute le suffixe
                        String description = column.get("label").asString();
                        if (suffixeFound != null) {
                            description = description + " - " + suffixeFound;
                        }
                        marqueurDTO.setDescription(title + " - " + description);
                    }
                    marqueurDTO.setType(column.get("type").asString());
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
            // Récupère le mapping pour ce champ
            JsonNode mappingNode = mappings.get(champ.get("mapping").asString());
            if (mappingNode != null && mappingNode.has("languages")) {
                JsonNode languages = mappingNode.get("languages");

                // Crée un nouvel objet JSON
                ObjectNode wrappedOptions = JsonNodeFactory.instance.objectNode();

                // Ajoute les valeurs françaises si disponibles
                JsonNode valuesFr = languages.path("fr").path("values");
                if (!valuesFr.isMissingNode()) {
                    wrappedOptions.set("fr", valuesFr);
                }

                // Ajoute les valeurs anglaises si disponibles
                JsonNode valuesEn = languages.path("en").path("values");
                if (!valuesEn.isMissingNode()) {
                    wrappedOptions.set("en", valuesEn);
                }

                marqueurDTO.setOptions(wrappedOptions);
            }

        }
    }

    private void extractTableauNodesWithParents(JsonNode node, String parentTitle,
            List<Map.Entry<String, JsonNode>> tableauWithTitle) {
        if (node.isObject()) {
            JsonNode typeNode = node.get("type");
            JsonNode titreNode = node.get("titre");
            if (typeNode != null && "tableau".equals(typeNode.asString())) {
                // On suppose que le titre est dans le nœud parent direct
                String titre = (parentTitle != null) ? parentTitle : titreNode.asString();
                tableauWithTitle.add(Map.entry(titre, node));
            }

            String currentTitle = (titreNode != null) ? titreNode.asString() : parentTitle;

            // Parcourir les enfants de l'objet
            node.properties().forEach(
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
        for (MarqueurBO marqueurBo : marqueurs) {
            MarqueurDTO marqueurDTO = marqueursTransformer.bo2Dto(marqueurBo);
            marqueurDTO.setPkMarqueur(null);
            list.add(marqueurDTO);
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
    public JsonNode buildDemande(BuildDemandeFromMarqueursDTO buildDemandeFromMarqueursDTO) {
        // Configuration initiale
        String lastBuildId = demandesConfigHelperService.getLastBuildId();
        List<MarqueurDTO> marqueurs = getMarqueurs(lastBuildId);

        // Utilisation de ObjectMapper pour créer un ObjectNode
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode demandeNode = mapper.createObjectNode();

        // Iteration sur chaque entrée
        if (buildDemandeFromMarqueursDTO.getDonnees() != null) {
            buildDemandeFromMarqueursDTO.getDonnees().forEach((marqueur, valeur) -> {
                MarqueurDTO marqueurMatch = marqueurs.stream().filter(m -> m.getIdentifiant().equals(marqueur))
                        .findFirst().orElse(null);

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
        }

        // choix multiple
        if (buildDemandeFromMarqueursDTO.getDonneesChoixMultiple() != null) {
            buildDemandeFromMarqueursDTO.getDonneesChoixMultiple().forEach((marqueur, valeurs) -> {
                MarqueurDTO marqueurMatch = marqueurs.stream().filter(m -> m.getIdentifiant().equals(marqueur))
                        .findFirst().orElse(null);

                if (marqueurMatch != null) {
                    String chemin = marqueurMatch.getChemin();
                    chemin = chemin.replaceFirst("^contenu\\.", "");
                    String[] segments = chemin.split("\\.");

                    ObjectNode currentNode = demandeNode;

                    for (int i = 0; i < segments.length; i++) {
                        String segment = segments[i];

                        // Dernier segment → tableau
                        if (i == segments.length - 1) {
                            ArrayNode arrayNode = mapper.createArrayNode();
                            valeurs.forEach(arrayNode::add);
                            currentNode.set(segment, arrayNode);
                        } else {
                            // créer la branche si absente
                            if (!currentNode.has(segment) || !currentNode.get(segment).isObject()) {
                                currentNode.set(segment, mapper.createObjectNode());
                            }
                            currentNode = (ObjectNode) currentNode.get(segment);
                        }
                    }
                }
            });
        }

        if (buildDemandeFromMarqueursDTO.getDonneesTableaux() != null) {
            // Traitement des tableaux
            buildDemandeFromMarqueursDTO.getDonneesTableaux().forEach(tableau -> {
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
                            if ("choixMultiple".equals(type)) {
                                try {
                                    // Désérialisation de la liste sérialisée
                                    JsonNode parsedNode = mapper.readTree(value);

                                    if (parsedNode.isArray()) {
                                        itemNode.set(lastPath, parsedNode);
                                    } else {
                                        // Sécurité : si ce n'est pas un tableau, on met un tableau vide
                                        itemNode.set(lastPath, mapper.createArrayNode());
                                    }
                                } catch (JacksonException e) {
                                    // JSON invalide → tableau vide
                                    itemNode.set(lastPath, mapper.createArrayNode());
                                }
                            } else if (type.equals("adresse") || type.equals("adresseMC") || type.equals(
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
                                    itemNode.set(rootPath, newNode);
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

    /**
     * Permet de récupérer la trad pour un marqueur type "choix" donné. En FR on a la trad dans getMarqueurTrad, mais si
     * on veut récupérer la trad EN on va la chercher dans les options du marqueur
     *
     * @param demandeDTO
     * @param marqueurIdentifiant
     * @return
     */
    @Override
    public String getMarqueurChoixTradFrOuEn(DemandeDTO demandeDTO, String marqueurIdentifiant) {
        if ("en".equals(demandeDTO.getLangue())) {
            MarqueurDTO marqueurDTO = getMarqueur(demandeDTO.getConfigBuildId(), marqueurIdentifiant);

            return Optional.ofNullable(marqueurDTO).map(MarqueurDTO::getOptions).map(opts -> opts.get("en"))
                    .map(mapEn -> mapEn.get(demandeDTO.getMarqueur(marqueurIdentifiant))).map(JsonNode::asString)
                    .orElseGet(() -> demandeDTO.getMarqueurTrad(marqueurIdentifiant));
        }
        return demandeDTO.getMarqueurTrad(marqueurIdentifiant);
    }

    /**
     * Permet de récuperer la list des options possibles d'un champ d'une demande pour un formulaire
     *
     * @param demandeDTO
     *         la demande concernée (doit contenir une config et un buildId)
     * @param marqueurId
     *         c'est l'idPredixe dans la config du champ concerné
     * @throws DemarcheException
     *         si impossible de récupérer le buildId de la demande
     */
    @Override
    public List<MappingOptionDTO> recupererMappingOptions(DemandeDTO demandeDTO, String marqueurId) {
        String buildId = demandeDTO.getConfigBuildId();
        MarqueurDTO marqueurDTO = getMarqueur(buildId, marqueurId);
        return marqueurDTO != null && marqueurDTO.getOptions() != null && marqueurDTO.getOptions().has("fr")
                ? extractMappingOptions(marqueurDTO.getOptions().get("fr"))
                : Collections.emptyList();
    }

    /**
     * Permet de récuperer l'option correspondante à la valeur en entrée depuis la list des options possibles d'un champ
     * d'une demande pour un formulaire
     *
     * @param demandeDTO
     *         la demande concernée (doit contenir une config et un buildId)
     * @param marqueurId
     *         c'est l'idPredixe dans la config du champ concerné
     * @param valeur
     *         c'est la valeur qu'on veut rechercher dans la list des options
     * @throws DemarcheException
     *         si impossible de récupérer le buildId de la demande
     */
    @Override
    public Optional<MappingOptionDTO> recupererOptionDepuisValeur(DemandeDTO demandeDTO, String marqueurId,
            String valeur) {
        List<MappingOptionDTO> champOptions = recupererMappingOptions(demandeDTO, marqueurId);

        return Optional.ofNullable(champOptions).orElse(List.of()).stream().filter(Objects::nonNull)
                .filter(item -> Strings.CI.equals(item.originalName(), valeur)).findFirst();
    }

    private List<MappingOptionDTO> extractMappingOptions(JsonNode rootNode) {

        if (rootNode == null || !rootNode.isObject()) {
            return List.of();
        }

        List<MappingOptionDTO> results = new ArrayList<>();

        rootNode.properties().forEach(entry -> results.add(new MappingOptionDTO(entry.getKey(),
                entry.getValue() != null ? entry.getValue().asString() : StringUtils.EMPTY)));

        return results;
    }


}
