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
    public void copyOrGenerateMarqueurs(String lastBuildId, String buildId, List<String> modelPaths,
            JsonNode sections) {
        if (lastBuildId != null) {
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
            setMarqueursFromModelPaths(modelPaths, marqueurDTOS, buildId, sections);

            marqueursRepository.saveAll(marqueursTransformer.dtos2Bos(marqueurDTOS));
        }
    }

    @Override
    public void resetMarqueurs() {
        marqueursRepository.deleteAll();
        List<DemandeConfigBO> configs = demandesConfigService.getConfigsBO();
        for (DemandeConfigBO config : configs) {
            List<MarqueurDTO> marqueurDTOS = new ArrayList<>();

            setMarqueursFromModelPaths(
                    demandesConfigService.getModelPaths(config.getContenu().get("modelPaths").get("rechercheAvancee")),
                    marqueurDTOS, config.getBuildId(), config.getContenu().get("recap").get("sections"));
            marqueursRepository.saveAll(marqueursTransformer.dtos2Bos(marqueurDTOS));
        }
    }

    private void setMarqueursFromModelPaths(List<String> modelPaths, List<MarqueurDTO> marqueurDTOS, String buildId,
            JsonNode sections) {
        for (String modelPath : modelPaths) {
            String id = pathToCamelCase(modelPath);
            // si le marqueur est déjà présent (du précédent buildId par exemple), on ne génère pas le marqueur
            if (marqueurDTOS.stream().noneMatch(marqueurDTO -> id.equals(marqueurDTO.getIdentifiant()))) {
                MarqueurDTO marqueur = new MarqueurDTO();
                marqueur.setChemin(modelPath);
                marqueur.setIdentifiant(id);
                marqueur.setBuildId(buildId);
                marqueur.setDescription(getDescriptionFromTranslations(sections, modelPath));
                marqueurDTOS.add(marqueur);
            }
        }
    }

    private String getDescriptionFromTranslations(JsonNode sections, String modelPath) {
        String modifiedModelPath = modelPath;
        String suffixeFound = null;
        String[] possibleSuffixesToRemove = { "ligne1", "ligne2", "ligne3", "ville", "pays", "codePostal", "bic",
                "iban", "indicatif", "numero" };
        for (String suffix : possibleSuffixesToRemove) {
            String suffixDot = "." + suffix;
            if (modelPath.endsWith(suffixDot)) {
                suffixeFound = suffix;
                modifiedModelPath = modelPath.substring(0, modelPath.length() - suffixDot.length());
                break;
            }
        }
        for (JsonNode section : sections) {
            // section
            if (section.get("type").asText().equals("champs")) {
                for (JsonNode champ : section.get("champs")) {
                    if (champ.get("path").asText().equals(modifiedModelPath)) {
                        // si c'est un type particulier on ajoute le suffixe
                        String description = champ.get("label").asText();
                        if (suffixeFound != null) {
                            description = description + " - " + suffixeFound;
                        }
                        return description;
                    }
                }

            }
        }

        return null;
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
                    tableau.forEach(itemNode::put);
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
