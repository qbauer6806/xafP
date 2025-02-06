package mc.gouv.xaf.xaf12batch.marqueurs;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.xaf12batch.demandesconfig.DemandesConfigService;
import mc.gouv.xaf.xaf12batch.dto.DemandeConfigBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MarqueursService {

    @Autowired
    private MarqueursRepository marqueursRepository;

    @Autowired
    private MarqueursTransformer marqueursTransformer;

    @Autowired
    private DemandesConfigService demandesConfigService;

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
                    // si c'est un type particulier on ajoute le suffixe
                    String description = champ.get("label").asText();
                    if (suffixeFound != null) {
                        description = description + " - " + suffixeFound;
                    }
                    marqueurDTO.setDescription(description);
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
                marqueurDTO.setDescription(title);
                marqueurDTO.setType(tableau.get("type").asText());
                return marqueurDTO;
            }
            for (JsonNode column : tableau.get("columns")) {
                String path = column.get("path").asText();
                if (path.equals(modifiedModelPath.substring(modifiedModelPath.lastIndexOf('.') + 1))) {
                    // si c'est un type particulier on ajoute le suffixe
                    String description = column.get("label").asText();
                    if (suffixeFound != null) {
                        description = description + " - " + suffixeFound;
                    }
                    marqueurDTO.setDescription(title + " - " + description);
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


}
