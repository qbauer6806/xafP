package mc.gouv.sup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.sup.es.utils.EsSchemaUtils;
import mc.gouv.sup.sql.utils.SQLScriptsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static mc.gouv.sup.es.utils.EsSchemaUtils.getProjectDemandeRecap;

/**
 *
 * Classe permettant de générer le fichier sql des requetes de la configuration des champs et des catégories
 *
 * @author asouabni.ext
 *
 */
public class GenerateEsSchemaAndSQL {

    // Logger permettant de tracer l'execution
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateEsSchemaAndSQL.class);
    private static final String DEST_SQL_FILE_PATH = "configration-recherche{0}.sql";
    private static final String DEST_ES_MAPPINGS_FILE_PATH = "{0}-es-schema.json";
    private static final String DEFAULT_SQL_CONF_FILE_PATH = "C:\\Workspace\\xaf\\xaf-sup\\src\\main\\resources\\default-config.sql";
    private static final String LOG_SEPARATOR = "-------------------------------------------------------------------------------------------------------------";

    private static final String DEST_SQL_FILE_NAME = MessageFormat.format(
            DEST_SQL_FILE_PATH, new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
    private static Map<String, String> datas = new HashMap<>();

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

        LOGGER.info("Chemin du fichier à parser: {}", path);
        LOGGER.info("Schéma de la base de données: {}", schema);

        // Création du noeud root
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] recapMapData = Files.readAllBytes(Paths.get(path));
        JsonNode root = objectMapper.readTree(recapMapData);

        // Lancement des générations
        StringBuilder sqlBuilder = new StringBuilder();
        SQLScriptsUtils.generateSQLScripts(getProjectDemandeRecap(root), "NC", schema, sqlBuilder);
        String jsonEsSchema = EsSchemaUtils.generateEsMappings(path, schema, datas);

        // Ecriture dans les dossiers
        createSqlFile(sqlBuilder.toString(), schema);
        createEsSchemaFile(jsonEsSchema, schema);

    }

    private static void createSqlFile(String sqlScript, String schema) throws IOException {
        String destSqlFilePathStr = GenerateEsSchemaAndSQL.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        destSqlFilePathStr = destSqlFilePathStr.substring(1, destSqlFilePathStr.length() - 8) + DEST_SQL_FILE_NAME;
        LOGGER.info("Fichier SQL : {}", destSqlFilePathStr);
        Path destSqlFilePath = Paths.get(destSqlFilePathStr);

        Files.write(destSqlFilePath, Collections.singletonList("--Requête générées depuis la moulinette à partir des données du front"), StandardOpenOption.CREATE);
        Files.write(destSqlFilePath, sqlScript.getBytes(), StandardOpenOption.APPEND);

        LOGGER.info("Lecture du fichier SQL par défaut : {}", DEFAULT_SQL_CONF_FILE_PATH);
        Path defaultSQLConfFilePath = Paths.get(DEFAULT_SQL_CONF_FILE_PATH);
        byte[] encodedDefaultScript = Files.readAllBytes(defaultSQLConfFilePath);
        String defaultScript = new String(encodedDefaultScript);
        defaultScript = MessageFormat.format(defaultScript, schema);
        Files.write(destSqlFilePath, Collections.singletonList("--Configuration par défaut"), StandardOpenOption.APPEND);
        Files.write(destSqlFilePath, Collections.singletonList(defaultScript), StandardOpenOption.APPEND);

        LOGGER.info(LOG_SEPARATOR);
        LOGGER.info("Script Sql généré avec succès.");
        LOGGER.info(LOG_SEPARATOR);
    }

    private static void createEsSchemaFile(String jsonEsSchema, String schema) throws IOException {
        String destEsMappingsFilePathStr = GenerateEsSchemaAndSQL.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String destESMappingsFileName = MessageFormat.format(DEST_ES_MAPPINGS_FILE_PATH, schema);
        destEsMappingsFilePathStr = destEsMappingsFilePathStr.substring(1, destEsMappingsFilePathStr.length() - 8) + destESMappingsFileName;
        LOGGER.info("Fichier Mappings ES : {}", destEsMappingsFilePathStr);
        Path destEsMappingsFilePath = Paths.get(destEsMappingsFilePathStr);
        Files.write(destEsMappingsFilePath, jsonEsSchema.getBytes(), StandardOpenOption.CREATE);
    }
}
