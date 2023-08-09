package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.util.FileServletUtils;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

@MultipartConfig
public class DocHolderFileServlet extends AbstractAfServlet {
    private final static long serialVersionUID = -314577095316396789L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderFileServlet.class);
    private static final String serviceUrl = AfServletGouvPropertiesResolver.getPorteDocUrl() + "/file";

    /**
     * Méthode pour l'opération <b>getFile</b>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== " + req.getServletPath() + " doGet()");

        LOGGER.info("Vérification usager connecté");
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        LOGGER.info("Récupération du paramètre 'filename' dans l'url");
        String filename = req.getParameter("filename");
        if (StringUtils.isEmpty(filename)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        try {
            // TODO : traitements sur "filename" ? (encodage, entités etc...)
            URIBuilder uriBuilder = new URIBuilder(serviceUrl);
            uriBuilder.addParameter("filename", filename);

            LOGGER.info("Génération de la requête porte-document");
            Request serviceRequest = Request.Get(uriBuilder.build());
            serviceRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
            serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();

            LOGGER.info("Constitution de la réponse pour retour au client");
            Header contentDispositionHeader = serviceResponse.getFirstHeader(RequestConstant.CONTENT_DISPOSITION_HEADER);
            resp.setHeader(RequestConstant.CONTENT_DISPOSITION_HEADER, contentDispositionHeader.getValue());
            resp.setStatus(serviceResponse.getStatusLine().getStatusCode());
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());

        } catch (URISyntaxException e) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        } catch (IOException ioe) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Erreur interne");
            return;
        }

        LOGGER.info("====================== Fin " + req.getServletPath() + " doGet()");
    }

    /**
     * Méthode pour l'opération <b>saveFile</b>
     * Elle permet de sauvegarder un fichier dans le porte-document de l'utilisateur connecté
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== " + req.getServletPath() + " doPost()");

        LOGGER.info("Vérification usager connecté");
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        LOGGER.info("Vérification des paramètres envoyés");
        String typedoc = req.getParameter("typedoc");
        String preferedName = req.getParameter("preferedName");
        if (StringUtils.isEmpty(typedoc) && StringUtils.isEmpty(preferedName)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        LOGGER.info("Vérification du fichier");
        try {
            Collection<Part> parts = req.getParts();

            if (parts.isEmpty()) {
                AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
                return;
            } else if (parts.size() > 1) {
                AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
                return;
            }

            Part filePart = parts.iterator().next();

            // Récupération du nom du fichier à envoyer
            String filename = filePart.getSubmittedFileName();
            if (StringUtils.isEmpty(filename)) {
                AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.FICHIER_NOM_MANQUANT);
                return;
            }

            LOGGER.info("Vérification de la taille du fichier...");
            if (!FileServletUtils.tailleFichierValide(filePart)) {
                LOGGER.info("La taille du fichier depasse la taille max definie dans les propriétés (taille du fichier : {} B)", filePart.getSize());
                AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_FORBIDDEN, SharedMessages.FICHIER_TROP_GRAND);
                return;
            }

            LOGGER.info("Vérification du type pour le fichier {} ...", filename.replaceAll(SharedMessages.UNSAFE_CHARS, "_"));
            if (!FileServletUtils.estExtensionDansWhitelist(filename)) {
                LOGGER.info("Le type de fichier ne correspond pas aux types whitelistés ({})", FileServletUtils.getExtensionsWhitelist());
                AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.FICHIER_TYPE_EXTENTION_INVALIDE);
                return;
            }

            LOGGER.info("Création de la requête");
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                    .addPart("file", new InputStreamBody(filePart.getInputStream(), ContentType.create(filePart.getContentType()), filePart.getSubmittedFileName()))
                    .addTextBody("preferedName", preferedName)
                    .addTextBody("typedoc", typedoc);

            Request serviceRequest = Request.Post(serviceUrl);
            serviceRequest.body(entityBuilder.build());
            serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

            LOGGER.info("Envoi de la requête");
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            resp.setStatus(statusCode);
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());
        } catch (ServletException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents saveFile, la requête n'est pas de type multipart/form-data", e);
        } catch (UnsupportedOperationException | IOException ioe) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents saveFile", ioe);
        }

        LOGGER.info("====================== Fin " + req.getServletPath() + " doPost()");
    }

    /**
     * Méthode pour l'opération "deleteFile"
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("====================== " + req.getPathInfo() + " doDelete()");

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info("Vérification usager connecté");
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        LOGGER.info("Vérification des paramètres envoyés");
        String filename = req.getParameter("filename");
        if (StringUtils.isEmpty(filename)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        LOGGER.info("Préparation de la requête");
        Request serviceRequest = Request.Delete(serviceUrl);
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            serviceRequest.bodyString(mapper.writeValueAsString(Map.of("filename", filename)), ContentType.APPLICATION_JSON);
            LOGGER.info("Envoi de la requête");
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            resp.setStatus(statusCode);
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());
        } catch (UnsupportedOperationException | IOException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents deleteFile", e);
        }

        LOGGER.info("====================== Fin " + req.getPathInfo() + " doDelete()");
    }

    /**
     * Méthode pour l'opération <b>patchFile</b>
     */
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("====================== " + req.getPathInfo() + " doPatch()");

        LOGGER.info("Vérification usager connecté");
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        LOGGER.info("Vérification des paramètres envoyés");
        String filename = req.getParameter("filename");
        String typedoc = req.getParameter("typedoc");
        String preferedName = req.getParameter("preferedName");
        if (StringUtils.isEmpty(filename) || StringUtils.isEmpty(typedoc) || StringUtils.isEmpty(preferedName)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        Map<String, String> parameters = Map.of("filename", filename, "typedoc", typedoc, "preferedName", preferedName);

        try {
            LOGGER.info("Préparation de la requête");
            Request serviceRequest = Request.Patch(serviceUrl);
            serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());
            serviceRequest.bodyString(new ObjectMapper().writeValueAsString(parameters), ContentType.APPLICATION_JSON);

            LOGGER.info("Envoi de la requête");
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            resp.setStatus(statusCode);
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());
        } catch (JsonProcessingException jpe) {
            LOGGER.info("Erreur lors de la conversion des paramètres en json");
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } catch (UnsupportedOperationException | IOException ioe) {
            LOGGER.info("Erreur lors de l'envoi de la requête à Monguichet");
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }


        LOGGER.info("====================== Fin " + req.getPathInfo() + " doPatch()");
    }
}
