package mc.gouv.xaf.front.controller;

import static mc.gouv.xaf.front.util.DocHolderUtils.DOCHOLDER_CONSENT_NODE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doc-holder")
public class DocHolderController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderController.class);
    private static final String BEARER = "Bearer ";

    @Autowired
    private FrontGouvPropertiesResolver frontGouvPropertiesResolver;

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    /**
     * Permet de savoir si l'utilisateur courrant possède un porte-document ou non
     */
    @GetMapping
    protected ResponseEntity doGet(HttpServletRequest req) {
        LOGGER.info("====================== {} doGet()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        Request serviceRequest = Request.get(frontGouvPropertiesResolver.getPorteDocUrl());
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, BEARER + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            ClassicHttpResponse response = (ClassicHttpResponse) serviceRequest.execute().returnResponse();
            String contentType = response.getEntity().getContentType();

            LOGGER.info("====================== Fin {} doGet()", req.getServletPath());
            return ResponseEntity.status(response.getCode())
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents getDocumentHolder", e);
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Methode pour l'opération <b>createDocumentHolder</b> Elle permet la création d'un nouveau "document-holder" ou
     * "porte-document"
     */
    @PostMapping
    protected ResponseEntity doPost(HttpServletRequest req) {
        LOGGER.info("====================== {} doPost()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        Request serviceRequest = Request.post(frontGouvPropertiesResolver.getPorteDocUrl());
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, BEARER + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            ClassicHttpResponse serviceResponse = (ClassicHttpResponse) serviceRequest.execute().returnResponse();
            String contentType = serviceResponse.getEntity().getContentType();

            LOGGER.info("====================== Fin {} doPost()", req.getServletPath());
            return ResponseEntity.status(serviceResponse.getCode())
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents createDocumentHolder", e);
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Méthode pour l'opération <b>deleteDocumentHolder</b> Elle permet la destruction d'un porte-document et la
     * suppression des données de consentement TS.
     */
    @DeleteMapping
    protected ResponseEntity doDelete(HttpServletRequest req) {
        LOGGER.info("====================== {} doDelete()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        Request serviceRequest = Request.delete(frontGouvPropertiesResolver.getPorteDocUrl());
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, BEARER + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            LOGGER.info("Suppression du consentement du porte-documents côté TS");
            LOGGER.info("Récupération des données d'accès");
            AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
            if (access == null) {
                LOGGER.error("Impossible de récupérer l'AccessDTO pour l'utilisateur id {}", usagerInfosDTO.getId());
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                        SharedMessages.ERREUR_INTERNE);
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
            ClassicHttpResponse serviceResponse = (ClassicHttpResponse) serviceRequest.execute().returnResponse();
            String contentType = serviceResponse.getEntity().getContentType();

            LOGGER.info("====================== Fin {} doDelete()", req.getServletPath());
            return ResponseEntity.status(serviceResponse.getCode())
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents deleteDocumentHolder", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
