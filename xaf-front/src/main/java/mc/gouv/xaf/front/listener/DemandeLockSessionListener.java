package mc.gouv.xaf.front.listener;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SessionConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Listener pour intercepter les évènements de session créée ou détruite
 *
 * @author agaidi
 */
@Component
public class DemandeLockSessionListener implements HttpSessionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeLockSessionListener.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        /* aucun traitement particulier */
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {

        /*
         * à la destruction d'une session, si l'utilisateur était en modification sur une demande, alors enlever les
         * verrous
         */
        HttpSession httpSession = se.getSession();
        if (httpSession != null) {
            Integer modificationDemandeId = (Integer) httpSession.getAttribute(
                    SessionConstant.SESSION_MODIFICATION_DEMANDE_ID);
            Integer modificationDemandeUsagerId = (Integer) httpSession.getAttribute(
                    SessionConstant.SESSION_MODIFICATION_USAGER_ID);

            if (modificationDemandeId != null && modificationDemandeUsagerId != null) {
                AfApiClient afApiClient = xafFrontserverUtils.getAfApiClient();
                afApiClient.unlockDemande(modificationDemandeId, modificationDemandeUsagerId);
                LOGGER.info("DemandeLockSessionListener: Demande {} déverrouillée", modificationDemandeId);
            }
            httpSession.invalidate();
        }
    }
}
