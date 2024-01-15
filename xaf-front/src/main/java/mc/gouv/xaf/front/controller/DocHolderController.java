package mc.gouv.xaf.front.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static mc.gouv.xaf.front.util.DocHolderUtils.DOCHOLDER_CONSENT_NODE;

@Controller
@RequestMapping("/doc-holder")
public class DocHolderController extends AbstractXafController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderController.class);

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
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        Request serviceRequest = Request.Get(frontGouvPropertiesResolver.getPorteDocUrl());
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();

            LOGGER.info("====================== Fin {} doGet()", req.getServletPath());
            return ResponseEntity.status(serviceResponse.getStatusLine().getStatusCode())
                    .contentType(MediaType.valueOf(serviceResponse.getEntity().getContentType().getValue()))
                    .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents getDocumentHolder", e);
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Methode pour l'opération <b>createDocumentHolder</b>
     * Elle permet la création d'un nouveau "document-holder" ou "porte-document"
     */
    @PostMapping
    protected ResponseEntity doPost(HttpServletRequest req) {
        LOGGER.info("====================== {} doPost()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        Request serviceRequest = Request.Post(frontGouvPropertiesResolver.getPorteDocUrl());
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();

            LOGGER.info("====================== Fin {} doPost()", req.getServletPath());
            return ResponseEntity.status(serviceResponse.getStatusLine().getStatusCode())
                    .contentType(MediaType.valueOf(serviceResponse.getEntity().getContentType().getValue()))
                    .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents createDocumentHolder", e);
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Méthode pour l'opération <b>deleteDocumentHolder</b>
     * Elle permet la destruction d'un porte-document et la suppression des données de consentement TS.
     */
    @DeleteMapping
    protected ResponseEntity doDelete(HttpServletRequest req) {
        LOGGER.info("====================== {} doDelete()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        Request serviceRequest = Request.Delete(frontGouvPropertiesResolver.getPorteDocUrl());
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            LOGGER.info("Suppression du consentement du porte-documents côté TS");
            LOGGER.info("Récupération des données d'accès");
            AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
            if (access == null) {
                LOGGER.error("Impossible de récupérer l'AccessDTO pour l'utilisateur id {}", usagerInfosDTO.getId());
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
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

            LOGGER.info("====================== Fin {} doDelete()", req.getServletPath());
            return ResponseEntity.status(serviceResponse.getStatusLine().getStatusCode())
                    .contentType(MediaType.valueOf(serviceResponse.getEntity().getContentType().getValue()))
                    .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents deleteDocumentHolder", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
