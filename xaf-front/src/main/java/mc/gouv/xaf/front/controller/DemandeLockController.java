package mc.gouv.xaf.front.controller;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SessionConstant;
import mc.gouv.xaf.shared.SharedMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.Date;

/**
 * Servlet permettant de verrouiller/deverrouiller une demande en y aposant un timestamp. le timestamp correspond à la
 * durée de vie de la session
 *
 * @author agaidi
 */
@RestController
@RequestMapping("/demandelock")
public class DemandeLockController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeLockController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @PutMapping
    public ResponseEntity doPut(@RequestParam Integer demandeId, HttpServletRequest request) {

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        LOGGER.info("====================== /demandeLock doPut()");

        Integer usagerId = usagerInfosDTO.getId();

        AfApiClient afApiClient = getAfApiClient();

        if (demandeId != null) {
            /* gestion du lock */
            verrouillerDemande(request, afApiClient, usagerId, demandeId);

        }
        LOGGER.info("====================== Fin /demandeLock doPut()");

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity doDelete(@RequestParam Integer demandeId, HttpServletRequest request) {
        LOGGER.info("====================== /demandeLock doDelete()");

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        Integer usagerId = usagerInfosDTO.getId();
        AfApiClient afApiClient = getAfApiClient();

        if (demandeId != null) {
            /* gestion du lock */
            deverrouillerDemande(request, afApiClient, usagerId, demandeId);

        }

        LOGGER.info("====================== Fin /demandeLock doDelete()");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private void verrouillerDemande(HttpServletRequest request, AfApiClient afApiClient, Integer usagerId,
                                    Integer demandeId) {

        HttpSession httpSession = request.getSession(false);

        /* on unlock une autre demande eventuellement lockée par la session */
        if (httpSession != null) {
            Integer modificationDemandeId = (Integer) httpSession
                    .getAttribute(SessionConstant.SESSION_MODIFICATION_DEMANDE_ID);
            Integer modificationDemandeUsagerId = (Integer) httpSession
                    .getAttribute(SessionConstant.SESSION_MODIFICATION_USAGER_ID);

            if (modificationDemandeId != null && modificationDemandeUsagerId != null
                    && !demandeId.equals(modificationDemandeId)) {

                afApiClient.unlockDemande(modificationDemandeId, modificationDemandeUsagerId);

                LOGGER.info(
                        "DemandeLockServlet verrouillerDemande: Demande {} déverrouillée suite au verrouillage de la demande {}",
                        modificationDemandeId, demandeId);
            }

            /*
             * la demande sera lockée jusqu'à l'expiration de la session, cad l'instant présent + durée max d'inactivité
             * de la session plus une minute de marge.
             */
            Long timestampValue = Instant.now().toEpochMilli() + (httpSession.getMaxInactiveInterval() * 1000L)
                    + 60000L;
            /* on lock la demande */
            afApiClient.lockDemande(demandeId, usagerId, timestampValue);
            LOGGER.info("DemandeLockServlet verrouillerDemande: Demande {} verrouillée jusque {}", demandeId,
                    new Date(timestampValue));
            request.getSession().setAttribute(SessionConstant.SESSION_MODIFICATION_DEMANDE_ID, demandeId);
            request.getSession().setAttribute(SessionConstant.SESSION_MODIFICATION_USAGER_ID, usagerId);
        }
    }

    private void deverrouillerDemande(HttpServletRequest request, AfApiClient afApiClient, Integer usagerId,
                                      Integer demandeId) {

        HttpSession httpSession = request.getSession(false);

        if (httpSession != null) {
            Integer modificationDemandeId = (Integer) httpSession
                    .getAttribute(SessionConstant.SESSION_MODIFICATION_DEMANDE_ID);
            Integer modificationDemandeUsagerId = (Integer) httpSession
                    .getAttribute(SessionConstant.SESSION_MODIFICATION_USAGER_ID);

            /*
             * si la demande dont on a demandé l'annulation est toujours référencée au niveau session on la retire de la
             * session
             */
            if (modificationDemandeId != null && modificationDemandeUsagerId != null
                    && demandeId.equals(modificationDemandeId)) {

                httpSession.setAttribute(SessionConstant.SESSION_MODIFICATION_DEMANDE_ID, null);
                LOGGER.info("DemandeLockServlet deverrouillerDemande: Demande {} retirée de la session",
                        modificationDemandeId);
            }
            afApiClient.unlockDemande(demandeId, usagerId);
            LOGGER.info("DemandeLockServlet deverrouillerDemande: Demande {} déverrouillée", demandeId);
        }

    }
}
