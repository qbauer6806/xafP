package mc.gouv.sup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
public class GenerateSearchConfigFiles {

    // Logger permettant de tracer l'execution
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateSearchConfigFiles.class);

    private static final String INSERT_CHAMP_REQUEST_TEMPLATE = "INSERT INTO {0}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES (''{1}'', ''{2}'', ''{3}'', (select id from {0}.dem_recherche_cat_config where libelle = ''{4}''), ''{5}'');";
    private static final String INSERT_CATEGORY_REQUEST_TEMPLATE = "INSERT INTO {0}.dem_recherche_cat_config (libelle, editable) VALUES (''{1}'', ''{2}'');";
    private static final String SECTION_TO_PARSE = "projectDemandeRecap";
    private static final String DEST_SQL_FILE_PATH = "./target/configration-recherche{0}.sql";
    private static final String DEST_ES_MAPPINGS_FILE_PATH = "./target/{0}-es-schema.json";
    private static final String DEFAULT_SQL_CONF_FILE_PATH = "./src/main/resources/default-config.sql";
    private static final String ES_TEMPLATE_FILE_PATH = "./src/main/resources/ts-es-schema.json";
    private static final String ES_TEMPLATE_CHANGE_ME_CONTENU_TAG = "//CHANGE_ME_CONTENU";
    private static final String ES_TEMPLATE_CHANGE_ME_DATA_TAG = "//CHANGE_ME_DATA";
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private static final String PROPERTIES_ES_NODE_NAME = "properties";
    private static final String ES_PROPERTY_TYPE = "type";
    private static final String RECAP_CHAMP_TYPE = "type";
    private static final String RECAP_CHAMP_ADRESSE_LIGNE1 = "ligne1";
    private static final String RECAP_CHAMP_ADRESSE_LIGNE2 = "ligne2";
    private static final String RECAP_CHAMP_ADRESSE_LIGNE3 = "ligne3";
    private static final String RECAP_CHAMP_ADRESSE_CP = "codePostal";
    private static final String RECAP_CHAMP_ADRESSE_VILLE = "ville";
    private static final String RECAP_CHAMP_ADRESSE_PAYS = "pays";
    private static final String RECAP_CHAMP_PATH = "path";

    private static final Path destSqlFilePath = Paths.get(
            MessageFormat.format(DEST_SQL_FILE_PATH, new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date())));
    private static Map<String, String> datas = new HashMap<>();

    enum EsType {

        TEXT("text"),
        KEYWORD("keyword"),
        DATE("date");

        private String type;

        public static EsType getFromType(String type) {
            if (type != null) {
                for (EsType estype : values()) {
                    if (type.equals(estype.getType())) {
                        return estype;
                    }
                }
            }
            return null;
        }

        private EsType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

    }

    enum RecapChampType {

        CHAINE("chaine"),
        CHOIX("choix"),
        DATE("date"),
        ADRESSE("adresse");

        private String type;

        private RecapChampType(String type) {
            this.type = type;
        }

        public static RecapChampType getFromType(String type) {
            if (type != null) {
                for (RecapChampType recapChampType : values()) {
                    if (type.equals(recapChampType.getType())) {
                        return recapChampType;
                    }
                }
            }
            return CHAINE;
        }

        public String getType() {
            return type;
        }
    }

    public static void main(String[] args) throws Exception {

        LOGGER.info("Début de la génération des fichiers de configuration...");
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Le chemin du fichier à parser ne doit pas être null");
        }
        if (args.length < 2) {
            throw new IllegalArgumentException("Le schéma de la base de données ne doit pas être null");
        }

        String path = args[0];
        String schema = args[1];

        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                String[] dataKeyValue = args[i].split(":");
                datas.put("data." + dataKeyValue[0], dataKeyValue[1]);
            }
        }

        LOGGER.info("Chemin du fichier à parser: {0}", path);
        LOGGER.info("Schéma de la base de données: {0}", schema);

        generateConfigFiles(path, schema);

    }

    /**
     * 
     * Méthode permettant de générer les fichiers de configuration de la recherche avancée
     * @param path Chemin du fichier récapitulatif à parser
     * @param schema Schéma de la base de données
     * @throws Exception Exception suite à la génération des fichiers de configuration
     */
    @SuppressWarnings("unchecked")
    private static void generateConfigFiles(String path, String schema) throws Exception {
        LOGGER.info("Début de la génération des fichiers de configuration de la recherche avancée...");

        byte[] recapMapData = Files.readAllBytes(Paths.get(path));

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> listRecap = objectMapper.readValue(recapMapData, ArrayList.class);

        if (listRecap != null) {
            for (Map<String, Object> recap : listRecap) {
                if (recap.get("name").equals(SECTION_TO_PARSE)) {

                    List<LinkedHashMap<String, Object>> sectionsList = (List<LinkedHashMap<String, Object>>) recap
                            .get("sections");
                    if (sectionsList == null) {
                        throw new Exception("Le fichier recap ne contient pas les sections");
                    } else {
                        generateSqlScript(sectionsList, schema);
                        generateElasticSearchMappings(sectionsList, schema);
                    }

                }
            }
        }
    }

    /**
     * Méthode permettant de générer le fichier sql contenant les requetes insert de la configuration des champs et des catégories à partir du front
     * 
     * @param sectionsList Liste des sections
     * @param schema Schéma de la base de données
     * @throws IOException Exception Input/Output
     */
    @SuppressWarnings({ "unchecked" })
    private static void generateSqlScript(List<LinkedHashMap<String, Object>> sectionsList, String schema)
            throws IOException {

        LOGGER.info(
                "Début de la génération du fichier SQL de la configuration des champs et des catégories de la recherche avancée...");

        List<String> categoriesQueries = new ArrayList<>();
        List<String> champsQueries = new ArrayList<>();
        for (LinkedHashMap<String, Object> section : sectionsList) {

            categoriesQueries.add(MessageFormat.format(INSERT_CATEGORY_REQUEST_TEMPLATE, schema,
                    getColumnValue(section.get("titre")), FALSE));
            List<LinkedHashMap<String, Object>> champs = (List<LinkedHashMap<String, Object>>) section.get("champs");
            if (champs != null) {
                for (LinkedHashMap<String, Object> champ : champs) {

                    if (champ.get(RECAP_CHAMP_TYPE).toString().equals(RecapChampType.ADRESSE.getType())) {
                        fillAdressesQueries(champsQueries, champ, getColumnValue(section.get("titre")), schema);
                    } else {
                        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE,
                                getColumnValue(champ.get(RECAP_CHAMP_PATH)), getColumnValue(champ.get("label")),
                                getColumnValue(section.get("titre")), FALSE));
                    }

                }
            }

        }
        Files.write(destSqlFilePath,
                Arrays.asList("--Requête générées depuis la moulinette à partir des données du front"),
                StandardOpenOption.CREATE);
        Files.write(destSqlFilePath, categoriesQueries, StandardOpenOption.APPEND);
        Files.write(destSqlFilePath, champsQueries, StandardOpenOption.APPEND);

        byte[] encodedDefaultScript = Files.readAllBytes(Paths.get(DEFAULT_SQL_CONF_FILE_PATH));
        String defaultScript = new String(encodedDefaultScript);
        defaultScript = MessageFormat.format(defaultScript, schema);
        Files.write(destSqlFilePath, Arrays.asList("--Configuration par défaut"), StandardOpenOption.APPEND);
        Files.write(destSqlFilePath, Arrays.asList(defaultScript), StandardOpenOption.APPEND);

        LOGGER.info("Script Sql généré avec succès dans " + destSqlFilePath.toFile().getAbsolutePath());

    }

    /**
     * Méthode permettant de récupérer la valeur à inserer dans la colonne avec le bon formatage
     * 
     * @param jsonValue Valeur récupérée depuis le fichier json à parser
     * @return Valeur à inserer dans la requete insert
     */
    private static String getColumnValue(Object jsonValue) {
        if (jsonValue != null) {
            return ((String) jsonValue).replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
                    .replace("\b", "\\b").replace("\f", "\\f").replace("'", "''");
        }
        return (String) jsonValue;
    }

    /**
     * Méthode permettant de générer les requête du type de champ adresse
     * @param champsQueries List à remplir
     * @param champ Champ récupéré du fichier à parser
     * @param category Catégorie du champ (section)
     * @param schema Schema de la base de données
     */
    private static void fillAdressesQueries(List<String> champsQueries, LinkedHashMap<String, Object> champ,
            String category, String schema) {
        if (champsQueries == null) {
            champsQueries = new ArrayList<>();
        }

        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE,
                getColumnValue(champ.get(RECAP_CHAMP_ADRESSE_LIGNE1)), "Adresse ligne 1", getColumnValue(category),
                FALSE));
        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE,
                getColumnValue(champ.get(RECAP_CHAMP_ADRESSE_LIGNE2)), "Adresse ligne 2", getColumnValue(category),
                FALSE));
        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE,
                getColumnValue(champ.get(RECAP_CHAMP_ADRESSE_LIGNE3)), "Adresse ligne 3", getColumnValue(category),
                FALSE));
        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE,
                getColumnValue(champ.get(RECAP_CHAMP_ADRESSE_CP)), "Code postal", getColumnValue(category), FALSE));
        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE,
                getColumnValue(champ.get(RECAP_CHAMP_ADRESSE_VILLE)), "Ville", getColumnValue(category), FALSE));
        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE,
                getColumnValue(champ.get(RECAP_CHAMP_ADRESSE_PAYS)), "Pays", getColumnValue(category), FALSE));
    }

    /**
     * Méthode permettant de génrer le fichier de mappings d'elasticsearch
     * 
     * @param sectionsList Liste des sections
     * @param appName Nom de l'application
     * @throws IOException Exception Input/Output
     */
    @SuppressWarnings("unchecked")
    private static void generateElasticSearchMappings(List<LinkedHashMap<String, Object>> sectionsList, String appName)
            throws IOException {

        LOGGER.info("Début de la génération du mapping elasticsearch...");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode contenu = mapper.createObjectNode();
        Path destEsMappingsFilePath = Paths.get(MessageFormat.format(DEST_ES_MAPPINGS_FILE_PATH, appName));

        for (LinkedHashMap<String, Object> section : sectionsList) {

            List<LinkedHashMap<String, Object>> champs = (List<LinkedHashMap<String, Object>>) section.get("champs");
            if (champs != null) {
                for (LinkedHashMap<String, Object> champ : champs) {
                    if (champ != null) {

                        String champType = champ.get(RECAP_CHAMP_TYPE).toString();
                        if (champType.equals(RecapChampType.ADRESSE.getType())) {

                            buildJsonProperty(getPropertiesAsArray(champ, RECAP_CHAMP_ADRESSE_LIGNE1), champType,
                                    contenu, mapper, RECAP_CHAMP_ADRESSE_LIGNE1);
                            buildJsonProperty(getPropertiesAsArray(champ, RECAP_CHAMP_ADRESSE_LIGNE2), champType,
                                    contenu, mapper, RECAP_CHAMP_ADRESSE_LIGNE2);
                            buildJsonProperty(getPropertiesAsArray(champ, RECAP_CHAMP_ADRESSE_LIGNE3), champType,
                                    contenu, mapper, RECAP_CHAMP_ADRESSE_LIGNE3);
                            buildJsonProperty(getPropertiesAsArray(champ, RECAP_CHAMP_ADRESSE_CP), champType, contenu,
                                    mapper, RECAP_CHAMP_ADRESSE_CP);
                            buildJsonProperty(getPropertiesAsArray(champ, RECAP_CHAMP_ADRESSE_VILLE), champType,
                                    contenu, mapper, RECAP_CHAMP_ADRESSE_VILLE);
                            buildJsonProperty(getPropertiesAsArray(champ, RECAP_CHAMP_ADRESSE_PAYS), champType, contenu,
                                    mapper, RECAP_CHAMP_ADRESSE_PAYS);
                        } else {
                            buildJsonProperty(getPropertiesAsArray(champ, RECAP_CHAMP_PATH), champType, contenu, mapper,
                                    RECAP_CHAMP_PATH);

                        }
                    }

                }
            }

        }

        ObjectNode data = mapper.createObjectNode();

        for (Entry<String, String> dataEntry : datas.entrySet()) {
            buildJsonProperty(dataEntry.getKey().split("\\."), dataEntry.getValue(), data, mapper, RECAP_CHAMP_PATH);
        }

        byte[] encodedJsonTemplate = Files.readAllBytes(Paths.get(ES_TEMPLATE_FILE_PATH));
        String jsonTemplate = new String(encodedJsonTemplate);
        jsonTemplate = getJsonFromTemplate(jsonTemplate, contenu, ES_TEMPLATE_CHANGE_ME_CONTENU_TAG);
        jsonTemplate = getJsonFromTemplate(jsonTemplate, data, ES_TEMPLATE_CHANGE_ME_DATA_TAG);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        jsonTemplate = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(mapper.readValue(jsonTemplate.getBytes(), Object.class));
        Files.write(destEsMappingsFilePath, Arrays.asList(jsonTemplate), StandardOpenOption.CREATE);
        LOGGER.info(
                "Mappings elasticsearch généré avec succès dans " + destEsMappingsFilePath.toFile().getAbsolutePath());

    }

    private static String getJsonFromTemplate(String jsonTemplate, ObjectNode node, String tag) {
        String parsedMapping = node.toString().replaceFirst("\\{", "");
        int curlybraceLastIndex = parsedMapping.lastIndexOf("}");
        parsedMapping = new StringBuilder(parsedMapping).replace(curlybraceLastIndex, curlybraceLastIndex + 1, "")
                .append(",").toString();
        return jsonTemplate.replace(tag, parsedMapping);
    }

    private static String[] getPropertiesAsArray(LinkedHashMap<String, Object> champ, String recapPath) {
        return champ.get(recapPath).toString().split("\\.");
    }

    /**
     * Méthode permettant de générer le json d'un propriété elasticsearch
     * 
     * @param champProperties propriétés du champ du fichier récapitulatif du front
     * @param champType Type u champ du fichier récapitulatif du front
     * @param contenu Noeud json à alimenter avec le contenu de la propriété
     * @param mapper Mapper jackson
     * @param esMappingsKey Clé du fichier récapitulatif du front contenant le chemin le propriété
     */
    private static void buildJsonProperty(String[] champProperties, String champType, ObjectNode contenu,
            ObjectMapper mapper, String esMappingsKey) {

        ObjectNode node = contenu;
        if (champProperties != null && champProperties.length > 0) {

            for (int i = 0; i <= champProperties.length - 1; i++) {

                if (i == champProperties.length - 1) {
                    ObjectNode leafNode = mapper.createObjectNode();
                    String esType = getEsType(champType);
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

    private static String getEsType(String type) {

        if (EsType.getFromType(type) != null) {
            return type;
        }
        switch (RecapChampType.getFromType(type)) {
            case CHAINE:
                return EsType.TEXT.getType();
            case CHOIX:
                return EsType.TEXT.getType();
            case ADRESSE:
                return EsType.TEXT.getType();
            case DATE:
                return EsType.DATE.getType();
            default:
                return null;

        }
    }

    private static boolean isMissingNode(JsonNode node) {
        return node == null || node instanceof MissingNode;
    }

}
