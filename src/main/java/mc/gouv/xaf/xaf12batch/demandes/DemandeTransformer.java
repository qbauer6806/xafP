package mc.gouv.xaf.xaf12batch.demandes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.servicerest.caching.PaysNationalitesCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class DemandeTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeTransformer.class);

    public static final String DEFAULT_FRENCH_DATE_FORMAT = "dd/MM/yyyy";

    @Autowired
    private PaysNationalitesCache paysCache;

    public JsonNode getNodeFromPath(JsonNode contenu, String path) {
        String chemin = getCheminRelatif(path);
        return contenu.at(chemin);
    }

    public String getCheminRelatif(String path) {
        return path.replace("contenu.", "/").replace(".", "/");
    }

    public void setContenuTrad(JsonNode contenuTrad, JsonNode config) {
        JsonNode mappings = config.get("mappings");
        List<JsonNode> champsNodes = config.get("recap").findValues("champs");
        for (JsonNode champs : champsNodes) {
            for (JsonNode champ : champs) {
                if (!champ.get("type").asText().equals("tableau")) {
                    JsonNode mapping = champ.get("mapping");
                    String path = champ.get("path").asText();
                    processContenuTrad(contenuTrad, mappings, mapping, champ, path);
                }
            }
        }
        // récupérer aussi les champs tableau
        List<JsonNode> tableauxNodes = new ArrayList<>();
        extractTableauNodes(config.get("recap"), tableauxNodes);
        for (JsonNode tableau : tableauxNodes) {
            String rootPath = tableau.get("path").asText();
            // on regarde si dans le contenu on a une array correspondant à ce path
            JsonNode array = getNodeFromPath(contenuTrad, rootPath);
            for (JsonNode champ : tableau.get("columns")) {
                JsonNode mapping = champ.get("mapping");
                // il faut itérer sur chaque contenu du tableau
                for (int i = 0; i < array.size(); i++) {
                    String path = rootPath + "." + i + "." + champ.get("path").asText();
                    processContenuTrad(contenuTrad, mappings, mapping, champ, path);
                }

            }
        }
    }

    private void extractTableauNodes(JsonNode node, List<JsonNode> tableauNodes) {
        if (node.isObject()) {
            // Si le nœud est un objet JSON
            JsonNode columnsNode = node.get("columns");
            if (columnsNode != null && columnsNode.isArray()) {
                tableauNodes.add(node);
            }

            // Parcourir les enfants de l'objet
            node.fields().forEachRemaining(entry -> extractTableauNodes(entry.getValue(), tableauNodes));
        } else if (node.isArray()) {
            // Si le nœud est un tableau
            node.forEach(childNode -> extractTableauNodes(childNode, tableauNodes));
        }
    }

    private void processContenuTrad(JsonNode contenuTrad, JsonNode mappings, JsonNode mapping, JsonNode champ,
            String path) {
        // le champ a un mapping
        if (mapping != null) {
            // on récupère le champ correspondant dans le contenu s'il existe
            JsonNode enumKeyNode = getNodeFromPath(contenuTrad, path);
            if (enumKeyNode != null && !enumKeyNode.isNull()) {
                if (enumKeyNode.isArray()) {
                    // choix multiple
                    ObjectMapper objectMapper = new ObjectMapper();
                    ArrayNode arrayNodeValues = objectMapper.createArrayNode();
                    for (JsonNode element : enumKeyNode) {
                        String enumValue;
                        String enumKey = element.asText();
                        JsonNode enumFound = mappings.get(mapping.asText()).get("languages").get("fr").get("values")
                                .get(enumKey);
                        if (enumFound != null) {
                            // si on trouve l'enum, alors on récupère la valeur
                            enumValue = enumFound.asText();
                        } else {
                            // sinon cela veut dire que la traduction a déjà été effectuée du coup on peut réutiliser la valeur
                            enumValue = enumKey;
                        }
                        arrayNodeValues.add(enumValue);
                    }
                    setNodeValueArray(contenuTrad, path, arrayNodeValues);
                } else {
                    // choix
                    String enumValue = "";
                    String enumKey = enumKeyNode.asText();
                    JsonNode isDynamic = champ.get("isDynamic");
                    if (isDynamic != null && !isDynamic.asBoolean()) {
                        JsonNode enumFound = mappings.get(mapping.asText()).get("languages").get("fr").get("values")
                                .get(enumKey);
                        if (enumFound != null) {
                            // si on trouve l'enum, alors on récupère la valeur
                            enumValue = enumFound.asText();
                        } else {
                            // sinon cela veut dire que la traduction a déjà été effectuée du coup on peut réutiliser la valeur
                            enumValue = enumKey;
                        }
                    } else if (mapping.asText().equals("nationalites")) {
                        enumValue = StringUtils.isBlank(enumKey)
                                ? ""
                                : paysCache.get(enumKey, "fr") != null
                                        ? paysCache.get(enumKey, "fr").getNationalite()
                                        : enumKey;
                    } else if (mapping.asText().equals("pays")) {
                        enumValue = StringUtils.isBlank(enumKey)
                                ? ""
                                : paysCache.get(enumKey, "fr") != null
                                        ? paysCache.get(enumKey, "fr").getNom()
                                        : enumKey;
                    }
                    setNodeValue(contenuTrad, path, enumValue);
                }

            }
        } else if (champ.get("type").asText().equals("adresse")) {
            // le champ est de type adresse donc on doit remplacer le pays
            path += ".pays";
            JsonNode enumKeyNode = getNodeFromPath(contenuTrad, path);
            if (enumKeyNode != null && !enumKeyNode.isNull() && !enumKeyNode.isMissingNode()) {
                String enumKey = enumKeyNode.asText();
                String enumValue = StringUtils.isBlank(enumKey)
                        ? ""
                        : paysCache.get(enumKey, "fr") != null
                        ? paysCache.get(enumKey, "fr").getNom()
                                : enumKey;
                setNodeValue(contenuTrad, path, enumValue);
            }
        } else if (champ.get("type").asText().equals("date")) {
            JsonNode dateNode = getNodeFromPath(contenuTrad, path);
            if (dateNode != null && !dateNode.isNull()) {
                String date = dateNode.asText();
                setNodeValue(contenuTrad, path, changeDateStringFormat(date));
            }
        }
    }

    private String changeDateStringFormat(final String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return " ";
        }
        try {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern(DEFAULT_FRENCH_DATE_FORMAT));
        } catch (DateTimeParseException e) {
            // impossible de parser la date, elle est sûrement déjà au bon format
            return dateString;
        }
    }

    public void setNodeValue(JsonNode contenu, String path, String nouvelleValeur){
        // [contenu,donnee,demandeur,prenom]
        List<String> donneeExterneKeyArray = new ArrayList<>(Arrays.asList(path.split("\\.")));
        // [donnee,demandeur,prenom]
        donneeExterneKeyArray.removeFirst();
        //	 "[donnee,demandeur]" / field = prenom
        String field = donneeExterneKeyArray.removeLast();
        // "/donnee/demandeur"
        String p = "/" + String.join("/", donneeExterneKeyArray);
        // Vérifier si le nœud existe
        JsonNode targetNode = contenu.at(p);
        if (!targetNode.isMissingNode()) {
            ((ObjectNode) targetNode).put(field, nouvelleValeur);
        }
    }

    public void setNodeValueArray(JsonNode contenu, String path, ArrayNode nouvelleValeur) {
        // [contenu,donnee,demandeur,prenom]
        List<String> donneeExterneKeyArray = new ArrayList<>(Arrays.asList(path.split("\\.")));
        // [donnee,demandeur,prenom]
        donneeExterneKeyArray.removeFirst();
        //	 "[donnee,demandeur]" / field = prenom
        String field = donneeExterneKeyArray.removeLast();
        // "/donnee/demandeur"
        String p = "/" + String.join("/", donneeExterneKeyArray);
        ((ObjectNode) contenu.at(p)).put(field, nouvelleValeur);
    }

    public void changeChoixAdditionnel(JsonNode node) {
        if (node != null) {
            if (node.isObject()) {
                ObjectNode objectNode = (ObjectNode) node;
                for (Iterator<Map.Entry<String, JsonNode>> it = objectNode.fields(); it.hasNext(); ) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    JsonNode valueNode = entry.getValue();
                    if (valueNode.isObject() && valueNode.has("valeur") && valueNode.has("valeurExtra")) {
                        String valeur = valueNode.path("valeur").asText();
                        String valeurExtra = valueNode.path("valeurExtra")
                                .asText(null); // returns null if field not present
                        String finalValue = (valeurExtra != null && !valeurExtra.isEmpty()) ? valeurExtra : valeur;

                        // Replace the original object node with the resolved value
                        it.remove();
                        objectNode.put(entry.getKey(), finalValue);
                    } else {
                        // Process children nodes
                        changeChoixAdditionnel(valueNode);
                    }
                }
            } else if (node.isArray()) {
                for (JsonNode arrayNode : node) {
                    changeChoixAdditionnel(arrayNode);
                }
            }
        }

    }

    public void changeChoixMultiple(JsonNode config, JsonNode contenu) {
        if (contenu != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            List<JsonNode> champsNodes = config.get("recap").findValues("champs");
            for (JsonNode champs : champsNodes) {
                for (JsonNode champ : champs) {
                    JsonNode type = champ.get("type");
                    if ("choixMultiple".equals(type.asText())) {
                        ArrayNode arrayNodeValues = objectMapper.createArrayNode();
                        JsonNode mappingValues = champ.get("mappingValues");
                        String path = champ.get("path").asText();
                        JsonNode node = getNodeFromPath(contenu, path);
                        // si c'est une array ça veut dire que le format a déjà changé
                        if (node != null && !node.isNull() && !node.isArray()) {
                            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
                            while (fields.hasNext()) {
                                Map.Entry<String, JsonNode> field = fields.next();
                                String key = field.getKey();
                                JsonNode value = field.getValue();
                                // on regarde d'abord que la valeur est bien un boolean et true
                                if (value.isBoolean() && value.asBoolean()) {
                                    // chercher la key camelCase dans le champ
                                    for (JsonNode mapppingValue : mappingValues) {
                                        if (mapppingValue.get("camelKey").asText().equals(key)) {
                                            arrayNodeValues.add(mapppingValue.get("key").asText());
                                            break;
                                        }
                                    }
                                } else if (value.isTextual()) {
                                    // c'est un champ custom autre donc on met le libellé
                                    arrayNodeValues.add(value.asText());
                                }
                            }
                            // on remplace l'ancien noeud choixMultiple du contenu par la liste de string
                            int lastDotIndex = path.lastIndexOf('.');
                            String rootPath = path.substring(0, lastDotIndex);
                            String nodeName = path.substring(lastDotIndex + 1);
                            JsonNode rootNode = getNodeFromPath(contenu, rootPath);
                            // Remplacer le noeud existant avec le nouveau ArrayNode
                            ((ObjectNode) rootNode).set(nodeName, arrayNodeValues);
                        }
                    }
                }
            }
        }

    }

    public void changeTableauComplexe(JsonNode config, JsonNode contenu) {
        if (contenu != null) {
            List<JsonNode> tableauxNodes = new ArrayList<>();
            extractTableauNodes(config.get("recap"), tableauxNodes);
            for (JsonNode tableau : tableauxNodes) {
                String rootPath = tableau.get("path").asText();
                JsonNode array = getNodeFromPath(contenu, rootPath);
                for (JsonNode champ : tableau.get("columns")) {
                    JsonNode type = champ.get("type");
                    // si c'est un type complexe on fait la transformation
                    if (type != null) {
                        String key = champ.get("path").asText();
                        if (type.asText().equals("adresse")) {
                            setComplexElements(
                                    new String[] { "ligne1", "ligne2", "ligne3", "ville", "pays", "codePostal" }, key,
                                    array);
                        } else if (type.asText().equals("adresseMc")) {
                            setComplexElements(new String[] { "ligne1", "ligne2", "ligne3" }, key, array);
                        } else if (type.asText().equals("telephone")) {
                            setComplexElements(new String[] { "indicatif", "numero" }, key, array);
                        } else if (type.asText().equals("iban")) {
                            setComplexElements(new String[] { "iban", "bic" }, key, array);
                        }
                    }

                }
            }
        }
    }

    private void setComplexElements(String[] possibleSuffixes, String key, JsonNode array) {
        ObjectMapper mapper = new ObjectMapper();
        for (JsonNode element : array) {
            ObjectNode newNode = mapper.createObjectNode();
            for (String suffix : possibleSuffixes) {
                String suffixWithUpperCase = suffix.substring(0, 1).toUpperCase() + suffix.substring(1);
                String contenuKey = key + suffixWithUpperCase;
                JsonNode propertyNode = element.get(contenuKey);
                if (propertyNode != null && !propertyNode.isNull() && !propertyNode.isMissingNode()) {
                    // on ajoute dans la nouvelle structure
                    newNode.put(suffix, propertyNode.asText());
                    // et on supprime
                    ((ObjectNode) element).remove(contenuKey);
                }
            }
            // si le newNode n'est pas vide, on l'ajoute
            if (!newNode.isEmpty()) {
                ((ObjectNode) element).put(key, newNode);
            }
        }

    }



}
