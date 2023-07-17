package mc.gouv.xaf.servlet.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.vscan.shared.dto.ScanDTO;
import mc.gouv.vscan.shared.dto.ScanRequestDTO;
import mc.gouv.xaf.servlet.dto.FileUploadCompteurDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.el.PropertyNotFoundException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FileServletUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServletUtils.class);
    private static final String EXTENSIONS_WHITELIST = "EXTENSIONS_WHITELIST";
    private static final String MAX_TAILLE_FICHIER = "MAX_TAILLE_FICHIER";
    private static final String VSCAN_ACTIVATION = "VSCAN_ACTIVATION";
    private static final String SLASH = "/";

    public static boolean estExtensionDansWhitelist(String filename) {
        String[] filenameSplit = filename.split("\\.");
        String fileExtension = filenameSplit[filenameSplit.length - 1].toLowerCase();
        return getExtensionsWhitelist().contains(fileExtension);
    }

    public static List<String> getExtensionsWhitelist() {
        List<String> extensions = new ArrayList<>();
        PropertiesDTO extensionsProperty = AppFactoryServletFrontPropertiesCache.getFrontProperty(EXTENSIONS_WHITELIST);

        if (extensionsProperty != null) {
            String propertyString = extensionsProperty.getValue().replace("*.", "").replace(" ", "");
            String[] types = propertyString.split(",");
            Collections.addAll(extensions, types);
        }

        return extensions;
    }

    public static boolean tailleFichierValide(Part part) {
        PropertiesDTO propMaxTailleFichiers = AppFactoryServletFrontPropertiesCache.getFrontProperty(MAX_TAILLE_FICHIER);
        if (propMaxTailleFichiers == null) {
            throw new PropertyNotFoundException("La propriété obligatoire MAX_TAILLE_FICHIER ne semble pas définie");
        }

        int tailleMaxFichier = Integer.parseInt(propMaxTailleFichiers.getValue());
        // transformation B en MB
        int tailleMaxFichierMB = tailleMaxFichier * 1000000;

        return part.getSize() <= tailleMaxFichierMB;
    }

    public static boolean vscan(Part part0, String filename, HttpPost postRequest, HttpServletResponse response, ServletContext servletContext) throws IOException {
        // Varification de l'activation de VSCAN
        PropertiesDTO propActivationVscan = AppFactoryServletFrontPropertiesCache.getFrontProperty(VSCAN_ACTIVATION);
        if (propActivationVscan == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR, "La propriété obligatoire VSCAN_ACTIVATION ne semble pas définie");
            return false;
        }

        // Constitution de la requête
        boolean activationVscan = Boolean.parseBoolean(propActivationVscan.getValue());
        // Rajouter l'information si le fichier a été scanné par VSCAN ou pas
        postRequest.setHeader(AppFactoryServletUtils.FILE_METADATA_SCANEXECUTE, activationVscan + ""); // TODO : activatoinVscan.toString() non ?
        LOGGER.info("Activation de VSCAN: {}", activationVscan);

        if (activationVscan) {
            LOGGER.info("Appel à VSCAN...");

            String urlVscan = AfServletGouvPropertiesResolver.getVscanUrl();
            LOGGER.info("URL = {}", urlVscan);
            try (CloseableHttpClient clientVscan = HttpClientBuilder.create().build()) {
                MultipartEntityBuilder builderVscan = MultipartEntityBuilder.create();
                builderVscan.addPart("file", new InputStreamBody(part0.getInputStream(), ContentType.create(part0.getContentType()), part0.getSubmittedFileName()));

                ScanRequestDTO scanRequest = new ScanRequestDTO();
                scanRequest.setCodeAppli(servletContext.getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY));
                scanRequest.setFilename(filename);
                scanRequest.setEnduserAppModule(servletContext.getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY).toLowerCase() + "-frontserver");

                ObjectMapper mapper = new ObjectMapper();
                String scanRequestStr = mapper.writeValueAsString(scanRequest);
                builderVscan.addPart("scanRequest", new StringBody(scanRequestStr, ContentType.TEXT_PLAIN));

                HttpEntity multipartVscan = builderVscan.build();
                HttpPost postRequestVscan = new HttpPost(urlVscan);
                postRequestVscan.setEntity(multipartVscan);
                postRequestVscan.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + AfServletGouvPropertiesResolver.getVscanJwt());

                HttpResponse postResponseVscan = clientVscan.execute(postRequestVscan);
                String vscanResp = IOUtils.toString(postResponseVscan.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.info("VSCAN Response : {} ({})", postResponseVscan.getStatusLine(), vscanResp);

                ScanDTO scanDto = mapper.readValue(vscanResp, ScanDTO.class);
                if (!scanDto.isResult()) {
                    LOGGER.info("VSCAN a détecté le fichier comme vérolé, fin du traitement, pas d'upload dans FILE");
                    AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST, "Erreur: le fichier soumis semble corrompu");
                    return false;
                }
            }

            LOGGER.info("VSCAN n'a pas considéré le fichier soumis comme vérolé");
        }
        return true;
    }

    /**
     * Permet de parser le nom du fichier depuis le Path Info de la requête
     */
    public static String getFilename(String pathInfo) {
        String filename = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            filename = pathInfo.split(SLASH)[1];
        }
        return filename;
    }

    /**
     * Vérification du nombre de fichier uploadés sur la demande
     * @param usagersFileUploadCompteurs
     * @param session
     * @return true si la limite a été atteinte, false si il est toujours possible d'uploader
     */
    public static boolean limiteUploadAtteinte(Map<HttpSession, FileUploadCompteurDTO> usagersFileUploadCompteurs, HttpSession session) {
        FileUploadCompteurDTO compteurUpload = usagersFileUploadCompteurs.get(session);
        if (compteurUpload != null) {
            Duration duration = Duration.between(compteurUpload.getDatePremierUpload(), LocalDateTime.now());
            int tempsParIntervalle = Integer.parseInt(AfServletGouvPropertiesResolver.getTempsIntervalleUpload());
            int maxUploadParIntervalle = Integer.parseInt(AfServletGouvPropertiesResolver.getMaxUploadParIntervalle());

            if (compteurUpload.getCompteur() >= maxUploadParIntervalle && duration.toMillis() < tempsParIntervalle) {
                return true;
            } else if (duration.toMillis() > tempsParIntervalle) {
                // Supprimer le compteur en cas de dépassement
                usagersFileUploadCompteurs.remove(session);
            }
        }
        return false;
    }
}
