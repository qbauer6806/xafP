package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.servlet.dto.DocHolderFileSearchDTO;
import mc.gouv.xaf.servlet.dto.DocHolderInfoDTO;
import mc.gouv.xaf.servlet.dto.PageFileSearchResultDTO;
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
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static mc.gouv.xaf.servlet.util.DocHolderUtils.CONSENTING_NODE;
import static mc.gouv.xaf.servlet.util.DocHolderUtils.DOCHOLDER_CONSENT_NODE;

public class DocHolderServlet extends AbstractAfServlet {
    private static final long serialVersionUID = -314577095316396789L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderServlet.class);
    private static final String SERVICE_URL = AfServletGouvPropertiesResolver.getPorteDocUrl();

    /**
     * Permet de savoir si l'utilisateur courrant possède un porte-documents et a consenti <b>côté GU</b> ou non
     * <b>Retour :</b>
     * <ul>
     *     <li><em>200</em> si consentement GU+TS et porte-documents créé.</li>
     *     <li><em>404</em> si l'utilisateur n'a pas consenti GU+TS ou si porte-documents non créé.</li>
     * </ul>
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("====================== {} doGet()", req.getServletPath());

        ObjectMapper mapper = new ObjectMapper();
        DocHolderInfoDTO docHolderInfoDTO = new DocHolderInfoDTO();

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        try {
            LOGGER.info("Récupération du consentement côté TS");
            LOGGER.info("Récupération des données d'accès");
            AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
            if (access == null) {
                LOGGER.error("Impossible de récupérer l'AccessDTO pour l'utilisateur id {}", usagerInfosDTO.getId());
                AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
                return;
            }

            LOGGER.info("Déserialisation des données d'accès");
            boolean consentTS = false;
            boolean consentGU = false;
            JsonNode docholderConsentNode = access.getContenu().get(DOCHOLDER_CONSENT_NODE);
            if (docholderConsentNode != null) {
                consentTS = docholderConsentNode.findPath(CONSENTING_NODE).asBoolean();
            }

            LOGGER.info("Récupération du consentement côté GU");
            // TODO !!!!

            LOGGER.info("Récupération du nombre de documents stockés dans le porte-documents");
            DocHolderFileSearchDTO fileSearchDTO = new DocHolderFileSearchDTO();
            fileSearchDTO.setOperator(DocHolderFileSearchDTO.OperatorEnum.AND);

            Request searchServiceRequest = Request.Post(SERVICE_URL + "/search");
            searchServiceRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
            searchServiceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());
            searchServiceRequest.bodyString(mapper.writeValueAsString(fileSearchDTO), ContentType.APPLICATION_JSON);

            HttpResponse searchServiceResponse = searchServiceRequest.execute().returnResponse();
            int searchStatusCode = searchServiceResponse.getStatusLine().getStatusCode();
            if (searchStatusCode != 200) {
                LOGGER.error("Une erreur est survenue à l'appel du service /search du porte-document, code http retourné : {}", searchStatusCode);
                AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
                return;
            }
            PageFileSearchResultDTO searchServiceResult = mapper.readValue(searchServiceResponse.getEntity().getContent(), PageFileSearchResultDTO.class);

            LOGGER.info("Envoi de la réponse au front");
            Request serviceRequest = Request.Get(SERVICE_URL);
            serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            resp.setStatus(serviceResponse.getStatusLine().getStatusCode());

            docHolderInfoDTO.setDocumentCount(searchServiceResult.getTotalElements());
            docHolderInfoDTO.setConsenting(consentTS && consentGU);
            docHolderInfoDTO.setDocHolderCreated(statusCode == 200);

            resp.setStatus(statusCode);
            mapper.writeValue(resp.getOutputStream(), docHolderInfoDTO);
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
