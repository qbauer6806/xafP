package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.DocHolderFileDTO;
import mc.gouv.xaf.servlet.dto.DocHolderFilePostDTO;
import mc.gouv.xaf.servlet.dto.DocHolderFileUpdateDTO;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.util.FileServletUtils;
import mc.gouv.xaf.servlet.util.HttpRequestWithEntity;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@MultipartConfig
public class DocHolderFileServlet extends AbstractAfServlet {
    private static final long serialVersionUID = -314577095316396789L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderFileServlet.class);
    private static final String SERVICE_URL = AfServletGouvPropertiesResolver.getPorteDocUrl() + "/file";
    public static final String VERIFICATION_USAGER_CONNECTE = "Vérification usager connecté";

    public static final String FILENAME = "filename";

    /**
     * Méthode pour l'opération <b>getFile</b>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== {} doGet()", req.getServletPath());

        LOGGER.info(VERIFICATION_USAGER_CONNECTE);
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        LOGGER.info("Récupération du paramètre 'filename' dans l'url");
        String filename = req.getParameter(FILENAME);
        if (StringUtils.isEmpty(filename)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        try {
            URIBuilder uriBuilder = new URIBuilder(SERVICE_URL);
            uriBuilder.addParameter(FILENAME, filename);

            LOGGER.info("Envoi de la requête porte-document");
            HttpResponse serviceResponse = FileServletUtils.downloadFromDocHolder(SERVICE_URL, filename, usagerInfosDTO.getTokenInfo().getAccessToken());

            LOGGER.info("Constitution de la réponse pour retour au client");
            if (serviceResponse.getStatusLine().getStatusCode() == 200) {
                Header contentDispositionHeader = serviceResponse.getFirstHeader(RequestConstant.CONTENT_DISPOSITION_HEADER);
                resp.setHeader(RequestConstant.CONTENT_DISPOSITION_HEADER, contentDispositionHeader.getValue());
                resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            }
            resp.setStatus(serviceResponse.getStatusLine().getStatusCode());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());
        } catch (URISyntaxException e) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        } catch (UnsupportedOperationException | IOException | InterruptedException ioe) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Erreur interne");
            return;
        }

        LOGGER.info("====================== Fin {} doGet()", req.getServletPath());
    }

    /**
     * Méthode pour l'opération <b>saveFile</b>
     * Elle permet de sauvegarder un fichier dans le porte-document de l'utilisateur connecté
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== {} doPost()", req.getServletPath());

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        LOGGER.info(VERIFICATION_USAGER_CONNECTE);
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        LOGGER.info("Vérification des paramètres envoyés");
        DocHolderFilePostDTO filePostDTO;
        try {
            filePostDTO = mapper.readValue(req.getInputStream(), DocHolderFilePostDTO.class);
        } catch (IOException ioe) {
            LOGGER.error("Erreur lors de la déserialisation de la requête", ioe);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        if (StringUtils.isBlank(filePostDTO.getUrl()) || StringUtils.isBlank(filePostDTO.getPreferredName()) || StringUtils.isBlank(filePostDTO.getTypedoc())) {
            LOGGER.error("Champs manquant dans la requête");
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        LOGGER.info("Téléchargement du fichier depuis FILE, url = " + filePostDTO.getUrl());
        try {
            try (InputStream filestream = FileServletUtils.downloadFile(filePostDTO.getUrl(), false, usagerInfosDTO.getAccessId(), getServletContext(), resp)) {
                if (resp.getStatus() == HttpServletResponse.SC_OK && filestream != null) {
                    LOGGER.info("Téléchargement réussi");
                    String filename = FileServletUtils.getFilename(filePostDTO.getUrl());
                    LOGGER.info("Upload du fichier vers PorteDocument");
                    HttpResponse uploadResponse = FileServletUtils.uploadToDocHolder(
                            SERVICE_URL,
                            filestream,
                            usagerInfosDTO.getTokenInfo().getAccessToken(),
                            filename, filePostDTO.getTypedoc(),
                            filePostDTO.getPreferredName(),
                            filePostDTO.getEndOfValidity());

                    int statusCode = uploadResponse.getStatusLine().getStatusCode();
                    LOGGER.info("Code retour de l'upload dans PorteDocument : " + statusCode);
                    resp.setStatus(statusCode);
                    IOUtils.copy(uploadResponse.getEntity().getContent(), resp.getOutputStream());
                }
            }
        } catch (UnsupportedOperationException | IOException ioe) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents saveFile", ioe);
        }

        LOGGER.info("====================== Fin {} doPost()", req.getServletPath());
    }

    /**
     * Méthode pour l'opération "deleteFile"
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("====================== {} doDelete()", req.getServletPath());

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info(VERIFICATION_USAGER_CONNECTE);
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        LOGGER.info("Vérification des paramètres envoyés");
        DocHolderFileDTO fileDTO = null;
        try {
            fileDTO = mapper.readValue(req.getInputStream(), DocHolderFileDTO.class);
        } catch (IOException e) {
            LOGGER.error("Impossible de déserialiser la requête", e);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
        }

        if (fileDTO == null || StringUtils.isEmpty(fileDTO.getFilename())) {
            LOGGER.error("L'objet de requête est null");
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        LOGGER.info("Préparation de la requête");
        HttpClient client = HttpClientBuilder.create().build();
        HttpRequestWithEntity serviceRequest = new HttpRequestWithEntity("DELETE");
        serviceRequest.setURI(URI.create(SERVICE_URL));
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            StringEntity entity = new StringEntity(mapper.writeValueAsString(Map.of(FILENAME, fileDTO.getFilename())));
            serviceRequest.setEntity(entity);
            serviceRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

            LOGGER.info("Envoi de la requête");
            HttpResponse serviceResponse = client.execute(serviceRequest);

            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            resp.setStatus(statusCode);
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());
        } catch (UnsupportedOperationException | IOException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents deleteFile", e);
        }

        LOGGER.info("====================== Fin {} doDelete()", req.getServletPath());
    }

    /**
     * Méthode pour l'opération <b>patchFile</b>
     */
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("====================== {} doPatch()", req.getServletPath());

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info(VERIFICATION_USAGER_CONNECTE);
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        LOGGER.info("Vérification des paramètres envoyés");
        DocHolderFileUpdateDTO fileUpdateDTO = null;
        try {
            fileUpdateDTO = mapper.readValue(req.getInputStream(), DocHolderFileUpdateDTO.class);
        } catch (IOException e) {
            LOGGER.error("Impossible de déserialiser la requête", e);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
        }

        if (fileUpdateDTO == null) {
            LOGGER.error("L'objet de requête est null");
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        try {
            LOGGER.info("Préparation de la requête");
            Request serviceRequest = Request.Patch(SERVICE_URL);
            serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());
            //serviceRequest.body(multipartEntityBuilder.build());
            serviceRequest.bodyString(new ObjectMapper().writeValueAsString(fileUpdateDTO), ContentType.APPLICATION_JSON);

            LOGGER.info("Envoi de la requête");
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            resp.setStatus(statusCode);
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());
        } catch (JsonProcessingException jpe) {
            LOGGER.info("Erreur lors de la conversion des paramètres en json");
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } catch (UnsupportedOperationException | IOException ioe) {
            LOGGER.info("Erreur lors de l'envoi de la requête à Monguichet");
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin {} doPatch()", req.getServletPath());
    }
}
