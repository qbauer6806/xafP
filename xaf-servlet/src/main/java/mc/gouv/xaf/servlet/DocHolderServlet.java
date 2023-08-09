package mc.gouv.xaf.servlet;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DocHolderServlet extends AbstractAfServlet {
    private final static long serialVersionUID = -314577095316396789L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderServlet.class);
    private static final String serviceUrl = AfServletGouvPropertiesResolver.getPorteDocUrl();

    /**
     * Methode pour l'opération <b>createDocumentHolder</b>
     * Elle permet la création d'un nouveau "document-holder" ou "porte-document"
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("====================== " + req.getServletPath() + " doPost()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        Request serviceRequest = Request.Post(serviceUrl);
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();

            resp.setStatus(serviceResponse.getStatusLine().getStatusCode());
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());

        } catch (UnsupportedOperationException | IOException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents createDocumentHolder", e);
        }

        LOGGER.info("====================== Fin " + req.getServletPath() + " doPost()");
    }

    /**
     * Méthode pour l'opération <b>deleteDocumentHolder</b>
     * Elle permet la destruction d'un porte-document.
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("====================== " + req.getServletPath() + " doDelete()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        Request serviceRequest = Request.Delete(serviceUrl);
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();

            resp.setStatus(serviceResponse.getStatusLine().getStatusCode());
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());

        } catch (UnsupportedOperationException | IOException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents deleteDocumentHolder", e);
        }

        LOGGER.info("====================== Fin " + req.getServletPath() + " doDelete()");
    }
}
