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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final String ES_TEMPLATE_CHANGE_ME_TAG = "//CHANGE_ME";

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
        LOGGER.info("Chemin du fichier à parser: {0}", path);
        LOGGER.info("Schéma de la base de données: {0}", schema);

        generateSqlScript(path, schema);
        generateElasticSearchMappings(path, schema);

    }

    /**
     * Méthode permettant de générer le fichier sql contenant les requetes insert de la configuration des champs et des catégories à partir du front
     * 
     * @param path Chemin du fichier à parser
     * @param schema Schéma de la base de données
     * @throws Exception Exception declenchée si le fichier json ne contient pas de sections
     */
    @SuppressWarnings({ "unchecked" })
    private static void generateSqlScript(String path, String schema) throws Exception {

        LOGGER.info(
                "Début de la génération du fichier SQL de la configuration des champs et des catégories de la recherche avancée...");
        Path destFilePath = Paths.get(MessageFormat.format(DEST_SQL_FILE_PATH,
                new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date())));

        byte[] mapData = Files.readAllBytes(Paths.get(path));

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> listRecap = objectMapper.readValue(mapData, ArrayList.class);

        if (listRecap != null) {
            for (Map<String, Object> recap : listRecap) {
                if (recap.get("name").equals(SECTION_TO_PARSE)) {

                    List<LinkedHashMap<String, Object>> sectionsList = (List<LinkedHashMap<String, Object>>) recap
                            .get("sections");
                    if (recap.get("sections") == null) {
                        throw new Exception("Le fichier recap ne contient pas les sections");
                    }
                    if (sectionsList != null) {
                        List<String> categoriesQueries = new ArrayList<>();
                        List<String> champsQueries = new ArrayList<>();
                        for (LinkedHashMap<String, Object> section : sectionsList) {

                            categoriesQueries.add(MessageFormat.format(INSERT_CATEGORY_REQUEST_TEMPLATE, schema,
                                    getColumnValue(section.get("titre")), "false"));
                            List<LinkedHashMap<String, Object>> champs = (List<LinkedHashMap<String, Object>>) section
                                    .get("champs");
                            if (champs != null) {
                                for (LinkedHashMap<String, Object> champ : champs) {

                                    if (champ.get("type").toString().equals("adresse")) {
                                        fillAdressesQueries(champsQueries, champ, getColumnValue(section.get("titre")),
                                                schema);
                                    } else {
                                        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema,
                                                "true", getColumnValue(champ.get("path")),
                                                getColumnValue(champ.get("label")),
                                                getColumnValue(section.get("titre")), "false"));
                                    }

                                }
                            }

                        }
                        Files.write(destFilePath,
                                Arrays.asList("--Requête générées depuis la moulinette à partir des données du front"),
                                StandardOpenOption.CREATE);
                        Files.write(destFilePath, categoriesQueries, StandardOpenOption.APPEND);
                        Files.write(destFilePath, champsQueries, StandardOpenOption.APPEND);

                    }

                    byte[] encodedDefaultScript = Files.readAllBytes(Paths.get(DEFAULT_SQL_CONF_FILE_PATH));
                    String defaultScript = new String(encodedDefaultScript);
                    defaultScript = MessageFormat.format(defaultScript, schema);
                    Files.write(destFilePath, Arrays.asList("--Configuration par défaut"), StandardOpenOption.APPEND);
                    Files.write(destFilePath, Arrays.asList(defaultScript), StandardOpenOption.APPEND);

                    LOGGER.info("Script Sql généré avec succès dans " + destFilePath.toFile().getAbsolutePath());

                }
            }
        }

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
    @SuppressWarnings("rawtypes")
    private static void fillAdressesQueries(List<String> champsQueries, LinkedHashMap<String, Object> champ,
            String category, String schema) {
        if (champsQueries == null) {
            champsQueries = new ArrayList();
        }

        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, "true",
                getColumnValue(champ.get("ligne1")), "Adresse ligne 1", getColumnValue(category), "false"));
        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, "true",
                getColumnValue(champ.get("ligne2")), "Adresse ligne 2", getColumnValue(category), "false"));
        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, "true",
                getColumnValue(champ.get("ligne3")), "Adresse ligne 3", getColumnValue(category), "false"));
        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, "true",
                getColumnValue(champ.get("codePostal")), "Code postal", getColumnValue(category), "false"));
        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, "true",
                getColumnValue(champ.get("ville")), "Ville", getColumnValue(category), "false"));
        champsQueries.add(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, "true",
                getColumnValue(champ.get("pays")), "Pays", getColumnValue(category), "false"));
    }

    /**
     * Méthode permettant de génrer le fichier de mappings d'elasticsearch
     * 
     * @param path Chemin du fichier à parser
     * @throws JsonProcessingException Exception déclenchée si il ya des problèmes de parsing json
     * @throws IOException Exception Input/Output
     */
    private static void generateElasticSearchMappings(String path, String schema)
            throws JsonProcessingException, IOException {

        LOGGER.info("Début de la génération du mapping elasticsearch...");
        Path destFilePath = Paths.get(MessageFormat.format(DEST_ES_MAPPINGS_FILE_PATH, schema));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootEsMappingsTemplateNode = mapper.readTree(Paths.get(path).toFile());
        for (JsonNode recap : rootEsMappingsTemplateNode) {
            if (recap.get("name").asText().equals(SECTION_TO_PARSE)) {
                byte[] encodedJsonTemplate = Files.readAllBytes(Paths.get(ES_TEMPLATE_FILE_PATH));
                String jsonTemplate = new String(encodedJsonTemplate);
                String parsedMapping = recap.get("elasticMappings").toString().replaceFirst("\\{", "");
                int curlybraceLastIndex = parsedMapping.lastIndexOf("}");
                parsedMapping = new StringBuilder(parsedMapping)
                        .replace(curlybraceLastIndex, curlybraceLastIndex + 1, "").append(",").toString();
                jsonTemplate = jsonTemplate.replace(ES_TEMPLATE_CHANGE_ME_TAG, parsedMapping);
                jsonTemplate = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(mapper.readValue(jsonTemplate.getBytes(), Object.class));
                Files.write(destFilePath, Arrays.asList(jsonTemplate), StandardOpenOption.CREATE);
                LOGGER.info(
                        "Mappings elasticsearch généré avec succès dans " + destFilePath.toFile().getAbsolutePath());
            }
        }

    }

}
