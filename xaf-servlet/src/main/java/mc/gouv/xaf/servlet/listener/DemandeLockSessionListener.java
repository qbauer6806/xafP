package mc.gouv.xaf.servlet.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SessionConstant;

/**
 * Listener pour intercepter les évènements de session créée ou détruite
 *
 * @author agaidi
 */
public class DemandeLockSessionListener implements HttpSessionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeLockSessionListener.class);

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
            Integer modificationDemandeId = (Integer) httpSession
                    .getAttribute(SessionConstant.SESSION_MODIFICATION_DEMANDE_ID);
            Integer modificationDemandeUsagerId = (Integer) httpSession
                    .getAttribute(SessionConstant.SESSION_MODIFICATION_USAGER_ID);

            if (modificationDemandeId != null && modificationDemandeUsagerId != null) {
                AfApiClient afApiClient = AppFactoryServletUtils.getAfApiClient();
                afApiClient.unlockDemande(modificationDemandeId, modificationDemandeUsagerId);
                LOGGER.info("HttpSessionListenerImpl: Demande {} déverrouillée", modificationDemandeId);
            }
            httpSession.invalidate();
        }
    }
}
