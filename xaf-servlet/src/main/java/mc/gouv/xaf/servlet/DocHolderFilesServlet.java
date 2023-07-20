package mc.gouv.xaf.servlet;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

public class DocHolderFilesServlet extends AbstractAfServlet {
    private final static long serialVersionUID = -314577095316396789L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderFilesServlet.class);
    private static final String serviceUrl = AfServletGouvPropertiesResolver.getPorteDocUrl() + "/files";


    /**
     * Méthode pour l'opération <b>getMultipleFiles</b>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        String filenames = req.getParameter("filenames[]");
        String zipName = req.getParameter("zipName");
        if (StringUtils.isEmpty(zipName) || StringUtils.isEmpty(filenames)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        try {
            URIBuilder uriBuilder = new URIBuilder(serviceUrl)
                    .addParameter("filenames[]", filenames)
                    .addParameter("zipName", zipName);

            Request serviceRequest = Request.Get(uriBuilder.build());
            serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();

            resp.setStatus(serviceResponse.getStatusLine().getStatusCode());
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());

        } catch (URISyntaxException e) {
            LOGGER.info("Erreur lors de la création de l'URL :", e);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Erreur interne");
        } catch (IOException ioe) {
            LOGGER.info("Erreur interne : ", ioe);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Erreur interne");
        }
    }

    /**
     * Méthode pour l'opération <b>deleteMultipleFiles</b>
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        String filenames = req.getParameter("filenames");
        if (StringUtils.isEmpty(filenames)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        try {
            URIBuilder uriBuilder = new URIBuilder(serviceUrl).addParameter("filenames[]", filenames);

            Request serviceRequest = Request.Delete(uriBuilder.build());
            serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();

            resp.setStatus(serviceResponse.getStatusLine().getStatusCode());
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());

        } catch (URISyntaxException e) {
            LOGGER.info("Erreur lors de la création de l'URL ", e);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Erreur interne");
        }
    }
}
