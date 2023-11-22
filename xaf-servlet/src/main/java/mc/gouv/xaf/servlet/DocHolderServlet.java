package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
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

import static mc.gouv.xaf.servlet.util.DocHolderUtils.DOCHOLDER_CONSENT_NODE;

public class DocHolderServlet extends AbstractAfServlet {
    private static final long serialVersionUID = -314577095316396789L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderServlet.class);
    private static final String SERVICE_URL = AfServletGouvPropertiesResolver.getPorteDocUrl();

    /**
     * Permet de savoir si l'utilisateur courrant possède un porte-document ou non
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("====================== {} doGet()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        Request serviceRequest = Request.Get(SERVICE_URL);
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            resp.setStatus(serviceResponse.getStatusLine().getStatusCode());
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());
        } catch (UnsupportedOperationException | IOException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents getDocumentHolder", e);
        }

        LOGGER.info("====================== Fin {} doGet()", req.getServletPath());
    }

    /**
     * Methode pour l'opération <b>createDocumentHolder</b>
     * Elle permet la création d'un nouveau "document-holder" ou "porte-document"
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("====================== {} doPost()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        Request serviceRequest = Request.Post(SERVICE_URL);
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

        LOGGER.info("====================== Fin {} doPost()", req.getServletPath());
    }

    /**
     * Méthode pour l'opération <b>deleteDocumentHolder</b>
     * Elle permet la destruction d'un porte-document et la suppression des données de consentement TS.
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("====================== {} doDelete()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        Request serviceRequest = Request.Delete(SERVICE_URL);
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            LOGGER.info("Suppression du consentement du porte-documents côté TS");
            LOGGER.info("Récupération des données d'accès");
            AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
            if (access == null) {
                LOGGER.error("Impossible de récupérer l'AccessDTO pour l'utilisateur id {}", usagerInfosDTO.getId());
                AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
                return;
            }

            JsonNode docholderConsentNode = access.getContenu().get(DOCHOLDER_CONSENT_NODE);
            if (docholderConsentNode != null) {
                ((ObjectNode) access.getContenu()).remove(DOCHOLDER_CONSENT_NODE);

                AccessInputDTO accessInputDTO = new AccessInputDTO();
                accessInputDTO.setContenu(access.getContenu());

                LOGGER.info("Mise à jour des données d'accès");
                getAfApiClient().createOrUpdateAccess(usagerInfosDTO.getId(), accessInputDTO);
            }

            LOGGER.info("Suppression du porte-documents côté GU");
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            resp.setStatus(serviceResponse.getStatusLine().getStatusCode());
            resp.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), resp.getOutputStream());
        } catch (UnsupportedOperationException | IOException e) {
            resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents deleteDocumentHolder", e);
        }

        LOGGER.info("====================== Fin {} doDelete()", req.getServletPath());
    }
}
