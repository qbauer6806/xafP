package mc.gouv.sup.es.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

import mc.gouv.sup.es.enums.EsType;
import mc.gouv.sup.es.enums.RecapChampType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * Classe permettant de générer le fichier sql des requetes de la configuration des champs et des catégories
 *
 * @author asouabni.ext
 *
 */
public class EsSchemaUtils {

    // Logger permettant de tracer l'execution
    private static final Logger LOGGER = LoggerFactory.getLogger(EsSchemaUtils.class);

    private static final String DEST_SQL_FILE_PATH = "configration-recherche{0}.sql";
    private static final String DEST_ES_MAPPINGS_FILE_PATH = "{0}-es-schema.json";

    /**
     * Fichier de configuartion par défaut des mappings ES
     */
    private static final String ES_TEMPLATE_FILE_PATH = "./src/main/resources/ts-es-schema.json";

    /**
     * TAG indiquant où insérer le mapping du contenu de la demande
     */
    private static final String ES_TEMPLATE_CHANGE_ME_CONTENU_TAG = "//CHANGE_ME_CONTENU";
    private static final String ES_TEMPLATE_CHANGE_ME_DATA_TAG = "//CHANGE_ME_DATA";

    private static final String PROPERTIES_ES_NODE_NAME = "properties";
    private static final String ES_PROPERTY_TYPE = "type";
    private static final String RECAP_CHAMP_ADRESSE_LIGNE1 = "ligne1";
    private static final String RECAP_CHAMP_ADRESSE_LIGNE2 = "ligne2";
    private static final String RECAP_CHAMP_ADRESSE_LIGNE3 = "ligne3";
    private static final String RECAP_CHAMP_ADRESSE_CP = "codePostal";
    private static final String RECAP_CHAMP_ADRESSE_VILLE = "ville";
    private static final String RECAP_CHAMP_ADRESSE_PAYS = "pays";
    private static final String RECAP_CHAMP_IBAN_TITULAIRE = "titulaire";
    private static final String RECAP_CHAMP_IBAN_BIC = "bic";
    private static final String RECAP_CHAMP_IBAN_IBAN = "iban";
    private static final String RECAP_CHAMP_PATH = "path";
    private static final String RECAP_CHAMP_CAMELKEY = "camelKey";

    /**
     *
     * Méthode permettant de générer les fichiers de configuration de la recherche avancée
     * @param path Chemin du fichier récapitulatif à parser
     * @param schema Schéma de la base de données
     * @throws Exception Exception suite à la génération des fichiers de configuration
     */
    @SuppressWarnings("unchecked")
    public static String generateEsMappings(String path, String schema, Map<String, String> datas) throws Exception {
        LOGGER.info("Début de la génération des fichiers de configuration de la recherche avancée...");

        byte[] recapMapData = Files.readAllBytes(Paths.get(path));

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(recapMapData);
        ObjectNode contenu = objectMapper.createObjectNode();

        // Appel à la recherche résursive
        depthFirstSearchESMapping(getProjectDemandeRecap(root), "NC", contenu, objectMapper, schema);

        ObjectNode data = objectMapper.createObjectNode();

        // Ajout des datas
        for (Entry<String, String> dataEntry : datas.entrySet()) {
            buildJsonProperty(dataEntry.getKey().split("\\."), dataEntry.getValue(), data, objectMapper);
        }

        // Finalisation du mapping ES
        byte[] encodedJsonTemplate = Files.readAllBytes(Paths.get(ES_TEMPLATE_FILE_PATH));
        String jsonTemplate = new String(encodedJsonTemplate);
        jsonTemplate = getJsonFromTemplate(jsonTemplate, contenu, ES_TEMPLATE_CHANGE_ME_CONTENU_TAG);
        if (!datas.isEmpty()) {
            jsonTemplate = getJsonFromTemplate(jsonTemplate, data, ES_TEMPLATE_CHANGE_ME_DATA_TAG);
        } else {
            jsonTemplate = jsonTemplate.replaceAll(ES_TEMPLATE_CHANGE_ME_DATA_TAG, "");
        }
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        jsonTemplate = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readValue(jsonTemplate.getBytes(), Object.class));
        return jsonTemplate;
    }


    private static String getEscapedColumnValue(Object jsonValue) {
        if (jsonValue != null) {
            return ((String) jsonValue).replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
                    .replace("\b", "\\b").replace("\f", "\\f").replace("'", "''");
        }
        return null;
    }


    private static void depthFirstSearchESMapping(JsonNode node, String sectionTitle, ObjectNode contenu, ObjectMapper mapper, String schema) {
        if (node.get("titre") != null) {
            sectionTitle = getEscapedColumnValue(node.get("titre").textValue());
        }

        String type = (node.get("type") != null)? node.get("type").textValue() : null;

        if ("choixMultiple".equals(type)) {
            String pathChoixMultiple = getEscapedColumnValue(node.get("path").textValue());
            for(JsonNode choixNode : node.get("mappingValues")) {
                buildJsonProperty((pathChoixMultiple + "." + choixNode.get(RECAP_CHAMP_CAMELKEY).textValue()).split("\\."), RecapChampType.CHOIX.getType(), contenu, mapper);
            }
            return;
        }

        if ("tableau".equals(type)) {
            String pathTableau = getEscapedColumnValue(node.get("path").textValue());
            for(JsonNode column : node.get("columns")) {
                // TODO quick fix pour le bon fonctionnement, mais adresse à prendre en compte
                if (column.get("type") != null && !"adresse".equals(column.get("type").textValue())) {
                    buildJsonProperty((pathTableau + "." + column.get(RECAP_CHAMP_PATH).textValue()).split("\\."), RecapChampType.TABLEAU.getType(), contenu, mapper);
                }
            }
            return;
        }

        if ("adresse".equals(type)) {
            buildJsonProperty(getPropertiesAsArray(node, RECAP_CHAMP_ADRESSE_LIGNE1), type, contenu, mapper);
            buildJsonProperty(getPropertiesAsArray(node, RECAP_CHAMP_ADRESSE_LIGNE2), type, contenu, mapper);
            buildJsonProperty(getPropertiesAsArray(node, RECAP_CHAMP_ADRESSE_LIGNE3), type, contenu, mapper);
            buildJsonProperty(getPropertiesAsArray(node, RECAP_CHAMP_ADRESSE_CP), type, contenu, mapper);
            buildJsonProperty(getPropertiesAsArray(node, RECAP_CHAMP_ADRESSE_VILLE), type, contenu, mapper);
            buildJsonProperty(getPropertiesAsArray(node, RECAP_CHAMP_ADRESSE_PAYS), type, contenu, mapper);
            return;
        }

        if (node.get("iban") != null) {
            buildJsonProperty(getPropertiesAsArray(node, RECAP_CHAMP_IBAN_TITULAIRE), type, contenu, mapper);
            buildJsonProperty(getPropertiesAsArray(node, RECAP_CHAMP_IBAN_BIC), type, contenu, mapper);
            buildJsonProperty(getPropertiesAsArray(node, RECAP_CHAMP_IBAN_IBAN), type, contenu, mapper);
            return;
        }

        if (node.get("path") != null) {
            buildJsonProperty(getPropertiesAsArray(node, RECAP_CHAMP_PATH), type, contenu, mapper);
            return;
        }

        for (JsonNode child : node) {
            if (child.isContainerNode()) {
                depthFirstSearchESMapping(child, sectionTitle, contenu, mapper, schema);
            }
        }
    }

    private static String getJsonFromTemplate(String jsonTemplate, ObjectNode node, String tag) {
        String parsedMapping = node.toString().replaceFirst("\\{", "");
        int curlybraceLastIndex = parsedMapping.lastIndexOf("}");
        parsedMapping = new StringBuilder(parsedMapping).replace(curlybraceLastIndex, curlybraceLastIndex + 1, "")
                .append(",").toString();
        return jsonTemplate.replace(tag, parsedMapping);
    }

    private static String[] getPropertiesAsArray(JsonNode node, String recapPath) {
        return node.get(recapPath).textValue().split("\\.");
    }

    /**
     * Méthode permettant de générer le json d'un propriété elasticsearch
     *
     * @param champProperties propriétés du champ du fichier récapitulatif du front
     * @param champType Type u champ du fichier récapitulatif du front
     * @param contenu Noeud json à alimenter avec le contenu de la propriété
     * @param mapper Mapper jackson
     */
    private static void buildJsonProperty(String[] champProperties, String champType, ObjectNode contenu,
            ObjectMapper mapper) {

        ObjectNode node = contenu;
        if (champProperties != null && champProperties.length > 0) {

            for (int i = 0; i <= champProperties.length - 1; i++) {

                if (i == champProperties.length - 1) {
                    ObjectNode leafNode = mapper.createObjectNode();
                    String esType = EsType.getEsType(champType);
                    leafNode.put(ES_PROPERTY_TYPE, esType);
                    if (esType != null && esType.equals(EsType.TEXT.getType())) {
                        ObjectNode keyword = mapper.createObjectNode();
                        ObjectNode keywordField = mapper.createObjectNode();
                        keyword.put(ES_PROPERTY_TYPE, EsType.KEYWORD.getType());
                        keyword.put("ignore_above", 256);
                        keywordField.set("keyword", keyword);
                        leafNode.set("fields", keywordField);
                        leafNode.put("analyzer", "default");
                        leafNode.put("search_analyzer", "default_search");
                    }

                    node.set(champProperties[i], leafNode);
                } else {
                    if (isMissingNode(node.get(champProperties[i]))) {
                        ObjectNode propertiesNode = mapper.createObjectNode();
                        propertiesNode.set(PROPERTIES_ES_NODE_NAME, mapper.createObjectNode());
                        node.set(champProperties[i], propertiesNode);
                    }

                    node = (ObjectNode) node.get(champProperties[i]).get(PROPERTIES_ES_NODE_NAME);

                }

            }
        }
    }

    private static boolean isMissingNode(JsonNode node) {
        return node == null || node instanceof MissingNode;
    }


    public static JsonNode getProjectDemandeRecap(JsonNode root) {
        for (Iterator<JsonNode> it = root.elements(); it.hasNext(); ) {
            JsonNode node = it.next();
            if(node.get("name") != null && node.get("name").textValue().equals("projectDemandeRecap")) {
                return node;
            }
        }
        return root;
    }
}
