package mc.gouv.xaf.xaf12batch.file;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Service d'appel à FILE pour les démarches
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    private static final String FILENAME_DONNER_FILE_LOG_MESSAGE = "Filename à donner à FILE : {}";
    private static final String FILECLIENT_SAVE_FILE_LOG_MESSAGE = "FileClient.saveFile({}, {}, {})";
    private static final String SLASH_DELIMITER = "/";
    private static final String ERREUR_FILESERVICE_LOG_MESSAGE = "Erreur dans FileServiceImpl.saveFile()";
    private static final String DEBUT_FILE_SERVICE_GET_FILE = "Début FileService.getFile({}, {})";
    private static final String FIN_FILE_CLIENT_GET_FILE = "Fin FileClient.getFile({}, {}, {})";

    private static final String FILE_METADATA_DEMANDEID = "X-MC-DEMANDEID";
    private static final String FILE_METADATA_DEMANDESTATUT = "X-MC-DEMANDESTATUT";
    private static final String FILE_METADATA_SCANEXECUTE = "X-MC-SCANEXECUTE";

    @Autowired
    private FileClient fileClient;


    @Value("${application.name}")
    private String applicationName;

    private ResponseEntity<InputStream> getFileEntity(String filename) throws IOException {
        LOGGER.info(DEBUT_FILE_SERVICE_GET_FILE, filename, "ROOT");
        String accountId = applicationName.toUpperCase();
        // Remplacement des espaces par des "+"...
        filename = filename.replace(" ", "+");
        ResponseEntity<InputStream> fileEntity = fileClient
                .getFileEntity(accountId, "ROOT", filename);
        LOGGER.info(FIN_FILE_CLIENT_GET_FILE, accountId, "ROOT", filename);
        return fileEntity;
    }

    private String saveFile(Integer fkAccess, String pkDemande, String dernierStatut, String filename, String contentType,
            InputStream inputStream) {
        // Définition de la meta pour le demande ID
        // On part du principe que le fichier a été généré côté back et n'est pas malicieux
        Map<String, String> customHeaders = createCustomHeaders(pkDemande, dernierStatut);

        filename = fkAccess + SLASH_DELIMITER + UUID.randomUUID() + SLASH_DELIMITER + filename;

        LOGGER.info(FILENAME_DONNER_FILE_LOG_MESSAGE, filename);

        String accountId = applicationName.toUpperCase();
        LOGGER.info(FILECLIENT_SAVE_FILE_LOG_MESSAGE, accountId, "ROOT", filename);
        try {
            return fileClient
                    .saveFile(accountId, "ROOT", inputStream, filename, contentType, customHeaders, null);
        } catch (Exception e) {
            LOGGER.error(ERREUR_FILESERVICE_LOG_MESSAGE, e);
            return null;
        }
    }

    private Map<String, String> createCustomHeaders(String pkDemandes, String dernierStatut) {
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put(FILE_METADATA_DEMANDEID, pkDemandes);
        customHeaders.put(FILE_METADATA_DEMANDESTATUT, dernierStatut);
        customHeaders.put(FILE_METADATA_SCANEXECUTE, true + "");
        return customHeaders;
    }

    public String dupliquerFichier(String fileUrl, String pkDemande, String dernierStatut) {
        try {
            FileInfo fileInfo = recupererFichierAvecContentType(fileUrl);

            try (InputStream is = fileInfo.inputStream()) {
                String[] parts = fileUrl.split("/");
                // on récupère le fkAccess
                String fkAccess = parts[1];

                return saveFile(Integer.parseInt(fkAccess), pkDemande, dernierStatut, URLEncoder.encode(fileInfo.fileName(), StandardCharsets.UTF_8), fileInfo.contentType(), is);
            }

        } catch (IOException e) {
            LOGGER.error("Erreur lors de la duplication du fichier : {}", fileUrl);
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    private FileInfo recupererFichierAvecContentType(String fileUrl) throws IOException {
        ResponseEntity<InputStream> response = getFileEntity(fileUrl);

        String contentType = response.getHeaders().getContentType() != null
                ? response.getHeaders().getContentType().toString()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return new FileInfo(extraireNomFichier(fileUrl), contentType, response.getBody());
    }

    private String extraireNomFichier(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
    }

    private record FileInfo(String fileName, String contentType, InputStream inputStream) {

    }



}
