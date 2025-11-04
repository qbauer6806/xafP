package mc.gouv.xaf.front.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.enums.HttpMethod;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SessionConstant;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Servlet mettant à disposition le service /demandes avec les méthodes PUT, POST, GET, DELETE. Cette servlet récupère
 * le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS correspondants dans le back-end générique.
 *
 * @author qdeme
 */
@Controller
@RequiredArgsConstructor
public class DemandesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesController.class);

    private final XafFrontserverUtils xafFrontserverUtils;

    private ResponseEntity traiterDemande(HttpMethod httpMethod, HttpServletRequest request, Integer demandeId,
            DemandeInputDTO demandeInput) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        AfApiClient afApiClient = xafFrontserverUtils.getAfApiClient();

        if (!HttpMethod.PUT.equals(httpMethod) && !HttpMethod.POST.equals(httpMethod)) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Mauvais appel de méthode.");
        }

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return ResponseEntity.internalServerError().build();
        }

        // Ajout des données externes MConnect si elles sont présentes (afin que l'API puisse les prendre en compte pour les places dans les bons endroits
        // du contenu de la demande. Ceci afin d'éviter un potentiel "hack" de la part de l'usager sur le FO)
        if (usagerInfosDTO.getDonneesExternes() != null
                && usagerInfosDTO.getDonneesExternes().get("mconnect") != null) {
            demandeInput.setDonneesMConnect(
                    mapper.treeToValue(usagerInfosDTO.getDonneesExternes().get("mconnect"), DonneesMConnectDTO.class));
        }

        DemandeDTO demandeDto;
        if (HttpMethod.POST.equals(httpMethod)) {
            LOGGER.info("Appel à la démarche pour créer la demande");
            if (request.getSession().getAttribute(SessionConstant.SESSION_DEMANDE_INITIALE) != null) {
                demandeInput.setContenuInitial(mapper.valueToTree(
                        request.getSession().getAttribute(SessionConstant.SESSION_DEMANDE_INITIALE)));
            }
            // récupérer la config pour faire les vérifications
            JsonNode config;
            try {
                config = xafFrontserverUtils.getConfig();
            } catch (IOException e) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                        "Impossible de lire le fichier config.json");
            }
            // vérifier la consistance des donneesexternes fournies par l'usager
            if (!xafFrontserverUtils.checkDonneesExternes(demandeInput.getDonneesExternes(),
                    config.get("donneesExternes"))) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                        "Inconsistance des donneesExternes envoyées");
            }
            demandeDto = afApiClient.creerDemande(demandeInput, usagerInfosDTO.getId());
            request.getSession().setAttribute(SessionConstant.SESSION_DEMANDE_INITIALE, null);
        } else {
            LOGGER.info("Appel à la démarche pour mettre à jour la demande {}", demandeId);
            demandeDto = afApiClient.updateDemande(demandeId, demandeInput, usagerInfosDTO.getId());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(demandeDto);
    }

    private ResponseEntity traiterDemandeInfoCompl(HttpServletRequest request, Integer demandeId,
            Integer demandeInfoComplId, DemandeComplementsReponseDTO response) {
        LOGGER.info("Appel à la démarche pour répondre à la demande d'informations complémentaires");
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return ResponseEntity.internalServerError().build();
        }

        DemandeComplementsDTO demandeComplement = xafFrontserverUtils.getAfApiClient()
                .repondreDemandeComplements(demandeId,
                demandeInfoComplId, response);

        return ResponseEntity.ok(demandeComplement);
    }

    @PostMapping(value = { "/demandes", "/demandes/{demandeId}" })
    public ResponseEntity doPostDemande(@PathVariable(required = false) Integer demandeId,
            @RequestBody(required = false) DemandeInputDTO demandeInput, HttpServletRequest request) {
        LOGGER.info("====================== /demandes doPost()");
        try {
            return traiterDemande(HttpMethod.POST, request, demandeId, demandeInput);
        } catch (JsonProcessingException e) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    "DemandesServlet - Une erreur est survenue lors de l'appel à la méthode " + HttpMethod.POST);
        }
    }

    @PutMapping("/demandes/{demandeId}/complements/{demandeInfoComplId}")
    public ResponseEntity doPutInfoCompl(@PathVariable Integer demandeId, @PathVariable Integer demandeInfoComplId,
            @RequestBody DemandeComplementsReponseDTO demandeInfoComplInput, HttpServletRequest request) {
        LOGGER.info("====================== /demandes doPost()");
        return traiterDemandeInfoCompl(request, demandeId, demandeInfoComplId, demandeInfoComplInput);
    }

    @PutMapping(value = { "/demandes", "/demandes/{demandeId}" })
    public ResponseEntity doPut(@PathVariable(required = false) Integer demandeId,
            @RequestBody(required = false) DemandeInputDTO demandeInput, HttpServletRequest request) {
        LOGGER.info("====================== /demandes doPut()");
        try {
            return traiterDemande(HttpMethod.PUT, request, demandeId, demandeInput);
        } catch (JsonProcessingException e) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    "DemandesServlet - Une erreur est survenue lors de l'appel à la méthode " + HttpMethod.PUT);
        }
    }

    @GetMapping("/demandes/{demandeId}")
    public ResponseEntity doGet(@PathVariable Integer demandeId, HttpServletRequest request) {
        LOGGER.info("====================== /demandes doGet()");

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return ResponseEntity.internalServerError().build();
        }

        LOGGER.info("Appel à la démarche pour récupérer la demande");
        DemandeDTO demandeDto = xafFrontserverUtils.getAfApiClient().getDemande(usagerInfosDTO.getId(), demandeId);

        return ResponseEntity.ok(demandeDto);
    }

    @GetMapping(value = { "/demandes/{demandeId}/recap" }, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> doGetRecap(@PathVariable Integer demandeId, HttpServletRequest request) {
        LOGGER.info("====================== /demandes doGetRecap()");

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return ResponseEntity.internalServerError().build();
        }

        DonneesMConnectDTO donneesMConnectDTO = null;
        ObjectMapper mapper = new ObjectMapper();
        if (usagerInfosDTO.getDonneesExternes() != null
                && usagerInfosDTO.getDonneesExternes().get("mconnect") != null) {
            try {
                donneesMConnectDTO = mapper.treeToValue(usagerInfosDTO.getDonneesExternes().get("mconnect"),
                        DonneesMConnectDTO.class);
            } catch (JsonProcessingException e) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        byte[] pdfBytes = xafFrontserverUtils.getAfApiClient()
                .getDemandeRecap(usagerInfosDTO.getId(), demandeId, donneesMConnectDTO);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfBytes);
    }

    @GetMapping("/demandes/{demandeId}/complements/{demandeInfoComplId}")
    public ResponseEntity doGet(@PathVariable Integer demandeId, @PathVariable Integer demandeInfoComplId,
            HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandes doGet()");

        AfApiClient afApiClient = xafFrontserverUtils.getAfApiClient();

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return ResponseEntity.internalServerError().build();
        }

        LOGGER.info("Appel à la démarche pour récupérer la demande d'informations complémentaires");
        DemandeComplementsDTO demandeComplementsDto = afApiClient.getDemandeComplements(demandeId, demandeInfoComplId);

        LOGGER.info("====================== Fin /demandes doGet()");
        return ResponseEntity.ok(demandeComplementsDto);
    }
}
