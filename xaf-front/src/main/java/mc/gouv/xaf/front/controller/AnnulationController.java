package mc.gouv.candifp.frontserver.movetoxaf.controller;

import mc.gouv.candifp.frontserver.movetoxaf.dto.UsagerInfosDTO;
import mc.gouv.candifp.frontserver.movetoxaf.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Servlet mettant à disposition le service /annulation avec la méthode POST, permettant
 * d'annuler une demande depuis le Front.
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/annulation")
public class AnnulationController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnulationController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    @DeleteMapping
    public ResponseEntity doDelete(HttpServletRequest request) {

        LOGGER.info("====================== /annulation doDelete()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        // Récupération de l'ID de la demande à annuler
        String pathInfo = request.getPathInfo();
        String demandeId = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            String[] pathElems = pathInfo.split("/");
            demandeId = pathElems[1];
        }
        if (demandeId == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_BAD_REQUEST,
                    "DemandeID non spécifié");
        }
        int demandeIdParsed;
        try {
            demandeIdParsed = Integer.parseInt(demandeId);
        } catch (NumberFormatException e) {
            LOGGER.error("Problème lors du parsing du demandeId");
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Problème lors du parsing du demandeId");
        }

        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = propertiesResolver.getDemarcheId();
        Integer usagerId = usagerInfosDTO.getId();
        LOGGER.info("DemarcheID={}, UsagerID={}, DemandeID={}", demarcheId, usagerId, demandeId);

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
