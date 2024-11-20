package mc.gouv.xaf.front.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import mc.gouv.xaf.front.dto.DocHolderFileDTO;
import mc.gouv.xaf.front.dto.DocHolderFilePostDTO;
import mc.gouv.xaf.front.dto.DocHolderFileUpdateDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.DocHolderUtils;
import mc.gouv.xaf.front.util.FileControllerUtils;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doc-holder/file")
@MultipartConfig
public class DocHolderFileController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderFileController.class);
    private static final String SERVICE_URL = "/file";
    public static final String VERIFICATION_USAGER_CONNECTE = "Vérification usager connecté";
    public static final String MAJ = "Mise à jour de la date de consentement TS du porte-documents";
    public static final String IMPOSSIBLE_MAJ = "Impossible de mettre à jour la date de consentement TS du porte-documents";
    public static final String VERIFICATION = "Vérification des paramètres envoyés";
    public static final String FILENAME = "filename";

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver frontGouvPropertiesResolver;

    @Autowired
    private FileControllerUtils fileControllerUtils;

    @Autowired
    private DocHolderUtils docHolderUtils;

    /**
     * Méthode pour l'opération <b>getFile</b>
     */
    @GetMapping
    protected ResponseEntity doGet(HttpServletRequest req) throws IOException {
        LOGGER.info("====================== {} doGet()", req.getServletPath());

        LOGGER.info(VERIFICATION_USAGER_CONNECTE);
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        LOGGER.info("Récupération du paramètre 'filename' dans l'url");
        String filename = req.getParameter(FILENAME);
        if (StringUtils.isEmpty(filename)) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    SharedMessages.REQUETE_MALFORMEE);
        }

        try {
            URIBuilder uriBuilder = new URIBuilder(frontGouvPropertiesResolver.getPorteDocUrl() + SERVICE_URL);
            uriBuilder.addParameter(FILENAME, filename);

            LOGGER.info("Envoi de la requête porte-document");
            ClassicHttpResponse serviceResponse = (ClassicHttpResponse) fileControllerUtils.downloadFromDocHolder(
                    frontGouvPropertiesResolver.getPorteDocUrl() + SERVICE_URL, filename,
                    usagerInfosDTO.getTokenInfo().getAccessToken());

            LOGGER.info("Constitution de la réponse pour retour au client");
            int statusCode = serviceResponse.getCode();
            ResponseEntity.BodyBuilder response = ResponseEntity.status(serviceResponse.getCode());
            if (statusCode == 200) {
                Header contentDispositionHeader = serviceResponse.getFirstHeader(
                        RequestConstant.CONTENT_DISPOSITION_HEADER);

                LOGGER.info(MAJ);
                if (!docHolderUtils.updateConsentDate(usagerInfosDTO.getId())) {
                    LOGGER.error(IMPOSSIBLE_MAJ);
                }
                response.header(RequestConstant.CONTENT_DISPOSITION_HEADER, contentDispositionHeader.getValue());
            }

            LOGGER.info("====================== Fin {} doGet()", req.getServletPath());

            return response.contentType(MediaType.valueOf(serviceResponse.getEntity().getContentType()))
                    .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

        } catch (URISyntaxException e) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    SharedMessages.REQUETE_MALFORMEE);
        } catch (UnsupportedOperationException | IOException e) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne");
        }
    }

    /**
     * Méthode pour l'opération <b>saveFile</b> Elle permet de sauvegarder un fichier dans le porte-document de
     * l'utilisateur connecté
     */
    @PostMapping
    protected ResponseEntity doPost(HttpServletRequest req) throws IOException {
        LOGGER.info("====================== {} doPost()", req.getServletPath());

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        LOGGER.info(VERIFICATION_USAGER_CONNECTE);
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        if (fileControllerUtils.limiteUploadAtteinte(usagerInfosDTO.getId())) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.FICHIER_LIMITE_UPLOAD_ATTEINTE);
        }
        LOGGER.info(VERIFICATION);
        DocHolderFilePostDTO filePostDTO;
        try {
            filePostDTO = mapper.readValue(req.getInputStream(), DocHolderFilePostDTO.class);
        } catch (IOException ioe) {
            LOGGER.error("Erreur lors de la déserialisation de la requête", ioe);
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    SharedMessages.REQUETE_MALFORMEE);
        }

        if (StringUtils.isBlank(filePostDTO.getUrl()) || StringUtils.isBlank(filePostDTO.getPreferredName())
                || StringUtils.isBlank(filePostDTO.getTypedoc())) {
            LOGGER.error("Champs manquant dans la requête");
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    SharedMessages.REQUETE_MALFORMEE);
        }

        LOGGER.info("Téléchargement du fichier depuis FILE, url = " + filePostDTO.getUrl());
        try {
            ResponseEntity<InputStream> responseFile = fileControllerUtils.downloadFile(filePostDTO.getUrl(), false,
                    usagerInfosDTO.getAccessId());
            try (InputStream filestream = responseFile.getBody()) {
                if (responseFile.getStatusCode() == HttpStatus.OK && filestream != null) {
                    LOGGER.info("Téléchargement réussi");
                    String filename = fileControllerUtils.getFilename(filePostDTO.getUrl());
                    LOGGER.info("Upload du fichier vers PorteDocument");
                    ClassicHttpResponse uploadResponse = (ClassicHttpResponse) fileControllerUtils.uploadToDocHolder(
                            frontGouvPropertiesResolver.getPorteDocUrl() + SERVICE_URL, filestream,
                            usagerInfosDTO.getTokenInfo().getAccessToken(), filename, filePostDTO.getTypedoc(),
                            filePostDTO.getPreferredName(), filePostDTO.getEndOfValidity());

                    int statusCode = uploadResponse.getCode();

                    if (statusCode == HttpStatus.OK.value()) {
                        LOGGER.info(MAJ);
                        if (!docHolderUtils.updateConsentDate(usagerInfosDTO.getId())) {
                            LOGGER.error(IMPOSSIBLE_MAJ);
                        }
                    }
                    fileControllerUtils.cleanLimiteUpload(usagerInfosDTO.getId());
                    LOGGER.info("====================== Fin {} doPost()", req.getServletPath());

                    return ResponseEntity.status(statusCode)
                            .body(new String(uploadResponse.getEntity().getContent().readAllBytes(),
                                    StandardCharsets.UTF_8));
                } else {
                    LOGGER.info("Erreur sur le téléchargement du fichier");
                    return ResponseEntity.internalServerError().build();
                }
            }
        } catch (UnsupportedOperationException | IOException ioe) {
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents saveFile", ioe);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Méthode pour l'opération "deleteFile"
     */
    @DeleteMapping
    protected ResponseEntity doDelete(HttpServletRequest req) {
        LOGGER.info("====================== {} doDelete()", req.getServletPath());

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info(VERIFICATION_USAGER_CONNECTE);
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        LOGGER.info(VERIFICATION);
        DocHolderFileDTO fileDTO = null;
        try {
            fileDTO = mapper.readValue(req.getInputStream(), DocHolderFileDTO.class);
        } catch (IOException e) {
            LOGGER.error("Impossible de déserialiser la requête", e);
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    SharedMessages.REQUETE_MALFORMEE);
        }

        if (fileDTO == null || StringUtils.isEmpty(fileDTO.getFilename())) {
            LOGGER.error("L'objet de requête est null");
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    SharedMessages.REQUETE_MALFORMEE);
        }

        LOGGER.info("Préparation de la requête");
        HttpClient client = HttpClientBuilder.create().build();

        HttpDelete serviceRequest = new HttpDelete(
                URI.create(frontGouvPropertiesResolver.getPorteDocUrl() + SERVICE_URL));
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            StringEntity entity = new StringEntity(mapper.writeValueAsString(Map.of(FILENAME, fileDTO.getFilename())),
                    StandardCharsets.UTF_8);
            serviceRequest.setEntity(entity);
            serviceRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

            LOGGER.info("Envoi de la requête");
            ClassicHttpResponse serviceResponse = (ClassicHttpResponse) client.execute(serviceRequest);
            int statusCode = serviceResponse.getCode();

            if (statusCode == HttpStatus.OK.value()) {
                LOGGER.info(MAJ);
                if (!docHolderUtils.updateConsentDate(usagerInfosDTO.getId())) {
                    LOGGER.error(IMPOSSIBLE_MAJ);
                }
            }

            LOGGER.info("====================== Fin {} doDelete()", req.getServletPath());
            return ResponseEntity.status(statusCode)
                    .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents deleteFile", e);
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Méthode pour l'opération <b>patchFile</b>
     */
    @PatchMapping
    protected ResponseEntity doPatch(HttpServletRequest req) {
        LOGGER.info("====================== {} doPatch()", req.getServletPath());

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info(VERIFICATION_USAGER_CONNECTE);
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }
        if (fileControllerUtils.limiteUploadAtteinte(usagerInfosDTO.getId())) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.FICHIER_LIMITE_UPLOAD_ATTEINTE);
        }
        LOGGER.info(VERIFICATION);
        DocHolderFileUpdateDTO fileUpdateDTO = null;
        try {
            fileUpdateDTO = mapper.readValue(req.getInputStream(), DocHolderFileUpdateDTO.class);
        } catch (IOException e) {
            LOGGER.error("Impossible de déserialiser la requête", e);
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    SharedMessages.REQUETE_MALFORMEE);
        }

        if (fileUpdateDTO == null) {
            LOGGER.error("L'objet de requête est null");
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    SharedMessages.REQUETE_MALFORMEE);
        }

        try {
            LOGGER.info("Préparation de la requête");
            Request serviceRequest = Request.patch(frontGouvPropertiesResolver.getPorteDocUrl() + SERVICE_URL);
            serviceRequest.setHeader(HttpHeaders.AUTHORIZATION,
                    "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());
            serviceRequest.bodyString(new ObjectMapper().writeValueAsString(fileUpdateDTO),
                    ContentType.APPLICATION_JSON);

            LOGGER.info("Envoi de la requête");
            ClassicHttpResponse serviceResponse = (ClassicHttpResponse) serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getCode();

            if (statusCode == HttpStatus.OK.value()) {
                LOGGER.info(MAJ);
                if (!docHolderUtils.updateConsentDate(usagerInfosDTO.getId())) {
                    LOGGER.error(IMPOSSIBLE_MAJ);
                }
            }
            fileControllerUtils.cleanLimiteUpload(usagerInfosDTO.getId());
            LOGGER.info("====================== Fin {} doPatch()", req.getServletPath());
            return ResponseEntity.status(statusCode)
                    .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

        } catch (JsonProcessingException jpe) {
            LOGGER.info("Erreur lors de la conversion des paramètres en json");
            return ResponseEntity.internalServerError().build();
        } catch (UnsupportedOperationException | IOException ioe) {
            LOGGER.info("Erreur lors de l'envoi de la requête à Monguichet");
            return ResponseEntity.internalServerError().build();
        }
    }
}
