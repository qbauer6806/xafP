package mc.gouv.xaf.front.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.UUID;
import mc.gouv.vscan.shared.dto.ScanDTO;
import mc.gouv.vscan.shared.dto.ScanRequestDTO;
import mc.gouv.xaf.front.dto.FileUploadResponseDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.FileControllerUtils;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.util.FileNameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Servlet servant à uploader un fichier dans FILE.
 *
 * @author qdeme
 */
@Controller
public class FileUploadController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);

    private static final String SLASH = "/";

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    @Autowired
    private FileControllerUtils fileControllerUtils;

    @PostMapping(value = { "/file" })
    public ResponseEntity<FileUploadResponseDTO> doPost(HttpServletRequest request) {
        LOGGER.info("====================== /fileupload doPost()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        if (fileControllerUtils.limiteUploadAtteinte(usagerInfosDTO.getId())) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.FICHIER_LIMITE_UPLOAD_ATTEINTE);
        }

        try {
            Part part = request.getParts().iterator().next();

            String filename = part.getSubmittedFileName();
            // Récupération du nom du fichier à envoyer
            if (StringUtils.isBlank(filename)) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                        "Erreur: nom du fichier manquant");
            }
            String safeFileName = FileNameUtils.getSafeFileName(XafFrontserverUtils.logSafe(filename));
            // Vérification de la conformité du fichier
            // Vérification du type du fichier
            LOGGER.info("Vérification du type pour le fichier {} ...", safeFileName);
            if (!fileControllerUtils.estExtensionDansWhitelist(safeFileName)) {
                LOGGER.info("Le type de fichier ne correspond pas aux types whitelistés, pas d'upload dans FILE");
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                        "Erreur: le type/extension du fichier soumis n'est pas valide");
            }

            LOGGER.info("Vérification de la taille...");
            // Vérification de la taille du fichier
            if (!fileControllerUtils.tailleFichierValide(part)) {
                LOGGER.info("La taille du fichier dépasse la taille max définie dans les propriétés");
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                        "Erreur: la taille du fichier depasse la taille max definie dans les propriétés");
            }

            // Vérification du vrai type MIME via Tika
            String mimeTypeFromExtension = new Tika().detect(safeFileName);

            try (InputStream inputStream = part.getInputStream();
                    TikaInputStream tikaInputStream = TikaInputStream.get(inputStream)) {

                Metadata metadata = new Metadata();
                metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, safeFileName);

                DefaultDetector detector = new DefaultDetector(new TikaConfig().getMimeRepository());
                MediaType mediaType = detector.detect(tikaInputStream, metadata);
                String detectedMimeType = mediaType.toString();

                if (!mimeTypeFromExtension.equals(detectedMimeType)) {
                    LOGGER.info("Le type MIME réel n'est pas celui de l'extension du fichier");
                    return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                            "Erreur: le type mime du fichier soumis n'est pas valide");
                }
            }

            // Génération de l'UUID
            UUID uuid = UUID.randomUUID();
            LOGGER.debug("UUID généré : {}", uuid);

            String accountId = propertiesResolver.getDemarcheId().toUpperCase();
            String containerId = XafFrontserverUtils.CONTAINER_ROOT;

            LOGGER.debug("accountId = {}, containerId = {}", accountId, containerId);

            // Récupération de l'AccessID via appel WS à Demarches
            LOGGER.info("Appel à la démarche pour récupérer l'AccessID correspondant..");
            AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
            Integer accessId = access.getPkAccess();
            LOGGER.debug("AccessID = {}", accessId);
            if (accessId == null) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.NOT_FOUND,
                        "Erreur: impossible de récupérer l'accès");
            }

            // Constitution du chemin virtuel du fichier
            // /appfactory/demarcheId/accessId/UUID/nomDuFichier
            String virtualPath =
                    SLASH + accountId + SLASH + containerId + SLASH + accessId + SLASH + uuid + SLASH + safeFileName;
            LOGGER.info("Chemin virtuel : {}", virtualPath);

            // Constitution de l'URL d'appel
            URI url = new URI(propertiesResolver.getFileUrl() + virtualPath);
            LOGGER.info("URL d'appel : {}", url);
            HttpPost postRequest = new HttpPost(url);

            // Appel à VSCAN afin d'effectuer le scan antivirus
            if (!vscan(part, safeFileName, postRequest)) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST, "Le fichier est corrompu");
            }

            // Extraction du demandeId si le client le connaît déjà et l'a fourni à AFS
            extraireDemandeId(postRequest, request);

            // Constitution de la requête
            HttpClient client = HttpClientBuilder.create().build();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("data",
                    new InputStreamBody(part.getInputStream(), ContentType.create(part.getContentType()),
                            part.getSubmittedFileName()));
            HttpEntity multipart = builder.build();
            postRequest.setEntity(multipart);
            postRequest.setHeader(HttpHeaders.AUTHORIZATION,
                    xafFrontserverUtils.getAuthHeader(XafFrontserverUtils.ServiceTarget.FILE));

            LOGGER.info("Appel du WS FILE");
            HttpResponse postResponse = client.execute(postRequest);

            fileControllerUtils.cleanLimiteUpload(usagerInfosDTO.getId());

            LOGGER.info("====================== Fin /fileupload doPost()");

            // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
            LOGGER.info("Constitution de la réponse pour retour au client");
            return constituerReponse(safeFileName, uuid, accessId, postResponse);

        } catch (Exception e) {
            LOGGER.error("FileUploadServlet - Une erreur est survenue lors de l'appel à la méthode POST", e);
            return ResponseEntity.status(getCodeErreur(e)).build();
        }

    }

    /**
     * Méthode permettant d'appeler VSCAN afin d'effectuer le scan antivirus.
     */
    private boolean vscan(Part part0, String filename, HttpPost postRequest) throws IOException {
        // Constitution de la requête
        boolean activationVscan = propertiesResolver.isVscanActivated();
        // Rajouter l'information si le fichier a été scanné par VSCAN ou pas
        postRequest.setHeader(XafFrontserverUtils.FILE_METADATA_SCANEXECUTE, activationVscan + "");
        LOGGER.info("Activation de VSCAN: {}", activationVscan);

        if (activationVscan) {
            LOGGER.info("Appel à VSCAN...");

            String urlVscan = propertiesResolver.getVscanUrl();
            LOGGER.info("URL = {}", urlVscan);
            HttpClient clientVscan = HttpClientBuilder.create().build();
            MultipartEntityBuilder builderVscan = MultipartEntityBuilder.create();
            builderVscan.addPart("file",
                    new InputStreamBody(part0.getInputStream(), ContentType.create(part0.getContentType()),
                            part0.getSubmittedFileName()));

            ScanRequestDTO scanRequest = new ScanRequestDTO();
            scanRequest.setCodeAppli(propertiesResolver.getDemarcheId());
            scanRequest.setFilename(filename);
            scanRequest.setEnduserAppModule(propertiesResolver.getDemarcheId().toLowerCase() + "-frontserver");

            ObjectMapper mapper = new ObjectMapper();
            String scanRequestStr = mapper.writeValueAsString(scanRequest);
            builderVscan.addPart("scanRequest", new StringBody(scanRequestStr, ContentType.TEXT_PLAIN));
            HttpEntity multipartVscan = builderVscan.build();
            HttpPost postRequestVscan = new HttpPost(urlVscan);
            postRequestVscan.setEntity(multipartVscan);
            postRequestVscan.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + propertiesResolver.getVscanJwt());
            ClassicHttpResponse postResponseVscan = (ClassicHttpResponse) clientVscan.execute(postRequestVscan);
            String vscanResp = IOUtils.toString(postResponseVscan.getEntity().getContent(), StandardCharsets.UTF_8);
            LOGGER.info("VSCAN Response : {} ({})", postResponseVscan.getCode(), vscanResp);

            ScanDTO scanDto = mapper.readValue(vscanResp, ScanDTO.class);
            if (!scanDto.isResult()) {
                LOGGER.info("VSCAN a détecté le fichier comme vérolé, fin du traitement, pas d'upload dans FILE");
                return false;
            }

            LOGGER.info("VSCAN n'a pas considéré le fichier soumis comme vérolé");
        }
        return true;
    }

    /**
     * Renseigne le demandeId dans la requête de création du fichier s'il est déjà connu
     */
    private void extraireDemandeId(HttpPost postRequest, HttpServletRequest request) {
        String demandeId = null;
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            if (headerName.startsWith(XafFrontserverUtils.FILE_METADATA_DEMANDEID)) {
                demandeId = request.getHeader(headerName);
            }
        }
        if (demandeId != null) {
            postRequest.setHeader(XafFrontserverUtils.FILE_METADATA_DEMANDEID, demandeId);
        }
    }

    /**
     * Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
     */
    private ResponseEntity<FileUploadResponseDTO> constituerReponse(String filename, UUID uuid, Integer accessId,
            HttpResponse postResponse) throws IOException {
        int statusCode = postResponse.getCode();
        ResponseEntity response;
        if (statusCode == HttpServletResponse.SC_OK || statusCode == HttpServletResponse.SC_CREATED) {
            // Si tout s'est bien passé, alors on forme une réponse différente que celle qui nous est retournée par FILE
            FileUploadResponseDTO responseObj = new FileUploadResponseDTO(
                    SLASH + accessId + SLASH + uuid + SLASH + filename);
            response = ResponseEntity.status(statusCode).body(responseObj);
        } else {
            LOGGER.error("Status code : {}", statusCode);
            // S'il y a eu un problème, alors on retourne le message d'erreur au client
            response = ResponseEntity.status(statusCode)
                    .body(((ClassicHttpResponse) postResponse).getEntity().getContent());
        }

        return response;
    }

}
