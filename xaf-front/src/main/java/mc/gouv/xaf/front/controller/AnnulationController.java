package mc.gouv.xaf.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Servlet mettant à disposition le service /annulation avec la méthode POST, permettant d'annuler une demande depuis le
 * Front.
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/annulation")
public class AnnulationController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnulationController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @DeleteMapping(value = { "/{demandeId}" })
    public ResponseEntity doDelete(@PathVariable(required = false) String demandeId, HttpServletRequest request) {

        LOGGER.info("====================== /annulation doDelete()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }
        if (demandeId == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_BAD_REQUEST, "DemandeID non spécifié");
        }
        int demandeIdParsed;
        try {
            demandeIdParsed = Integer.parseInt(demandeId);
        } catch (NumberFormatException e) {
            LOGGER.error("Problème lors du parsing du demandeId");
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Problème lors du parsing du demandeId");
        }

        Integer usagerId = usagerInfosDTO.getId();
        LOGGER.info("UsagerID={}, DemandeID={}", usagerId, demandeId);

        LOGGER.info("Appel à la démarche...");
        AfApiClient afApiClient = getAfApiClient();

        // Vérification si l'usager à le droit d'annuler cette demande
        try {
            afApiClient.getDemande(usagerId, demandeIdParsed);
        } catch (Exception exception) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        // Annulation de la demande
        try {
            afApiClient.annulerDemande(Integer.parseInt(demandeId), usagerId);
            LOGGER.info("Retour au client...");
            LOGGER.info("====================== Fin /annulation doDelete()");
            return ResponseEntity.ok().build();
        } catch (Exception exception) {
            LOGGER.error("AnnulationServlet - Une erreur est survenue lors de l'appel à la méthode DELETE", exception);
            return ResponseEntity.status(getCodeErreur(exception)).build();
        }

    }
}
