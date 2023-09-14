package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.util.FileServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

@MultipartConfig
public class DocHolderFilesServlet extends AbstractAfServlet {
    private static final long serialVersionUID = -314577095316396789L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderFilesServlet.class);
    private static final String SERVICE_URL = AfServletGouvPropertiesResolver.getPorteDocUrl() + "/files";
    public static final String FILENAMES = "filenames";
    public static final String ZIP_NAME = "zipName";
    public static final String FILENAMES_ARRAY = "filenames[]";


    /**
     * Méthode pour l'opération <b>getMultipleFiles</b>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== {} doGet()", req.getServletPath());
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        String filenames = req.getParameter(FILENAMES_ARRAY);
        String zipName = req.getParameter(ZIP_NAME);
        if (StringUtils.isEmpty(zipName) || StringUtils.isEmpty(filenames)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        try {
            URIBuilder uriBuilder = new URIBuilder(SERVICE_URL)
                    .addParameter(FILENAMES_ARRAY, filenames)
                    .addParameter(ZIP_NAME, zipName);

            Request serviceRequest = Request.Get(uriBuilder.build());
            serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();

            resp.setStatus(serviceResponse.getStatusLine().getStatusCode());
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());

        } catch (URISyntaxException e) {
            LOGGER.info("Erreur lors de la création de l'URL :", e);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
        } catch (UnsupportedOperationException | IOException ioe) {
            LOGGER.info("Erreur interne : ", ioe);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
        }
        LOGGER.info("====================== Fin {} doGet()", req.getServletPath());
    }

    /**
     * Méthode pour le transfert de fichiers depuis FILE à MonGuichet.
     *
     * @param req  an {@link HttpServletRequest} object that
     *             contains the request the client has made
     *             of the servlet
     * @param resp an {@link HttpServletResponse} object that
     *             contains the response the servlet sends
     *             to the client
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== {} doPost()", req.getServletPath());


        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        String[] filenames = mapper.readValue(req.getInputStream(), String[].class);
        if (filenames == null || filenames.length == 0) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        LOGGER.info("Démarrage du téléchargement des fichiers ({} au total)", filenames.length);
        for (String filename : filenames) {
            if (!StringUtils.isEmpty(filename)) {
                LOGGER.info("Téléchargement du fichier {} dans FILE", filename);
                try (InputStream fileInput = FileServletUtils.downloadFile(filename, false, usagerInfosDTO.getAccessId(), getServletContext(), resp)) {

                    if (fileInput == null) {
                        AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Impossible de télécharger le fichier " + filename);
                        return;
                    }

                    Request serviceRequest = Request.Post(SERVICE_URL);
                    serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

                    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().addPart("file", new InputStreamBody(fileInput, filename));
                    serviceRequest.body(entityBuilder.build());

                    LOGGER.info("Envoi du fichier {} dans Monguichet", filename);
                    HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
                    int serviceResponseStatus = serviceResponse.getStatusLine().getStatusCode();
                    if (serviceResponseStatus != HttpStatus.SC_OK) {
                        AppFactoryServletUtils.logAndSendError(LOGGER, resp, serviceResponseStatus, "Une erreur est survenue lors de l'envoi du fichier " + filename + " à monguichet");
                        return;
                    }
                }
                LOGGER.info("Fin du téléchargement du fichier {}", filename);
            }
        }
        LOGGER.info("Fin du téléchargement des fichiers");

        LOGGER.info("====================== Fin {} doPost()", req.getServletPath());
    }

    /**
     * Méthode pour l'opération <b>deleteMultipleFiles</b>
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("====================== {} doDelete()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        String filenames = req.getParameter(FILENAMES);
        if (StringUtils.isEmpty(filenames)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        try {
            URIBuilder uriBuilder = new URIBuilder(SERVICE_URL).addParameter(FILENAMES_ARRAY, filenames);

            Request serviceRequest = Request.Delete(uriBuilder.build());
            serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();

            resp.setStatus(serviceResponse.getStatusLine().getStatusCode());
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());

        } catch (URISyntaxException e) {
            LOGGER.info("Erreur lors de la création de l'URL ", e);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.info("Erreur lors de la lecture de la réponse monguichet ", e);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
        }

        LOGGER.info("====================== Fin {} doDelete()", req.getServletPath());
    }
}
