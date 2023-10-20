package mc.gouv.xaf.servlet.filter;

import java.io.IOException;
import java.time.Instant;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SessionConstant;

public class DemandeLockFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeLockFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        /* Aucun traitement particulier */
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        /*
         * à l'interception d'une request, si l'utilisateur était en modification sur une demande, alors prolonger le
         * verrou. On veillera à ne pas prolonger plus d'une fois par minute pour preserver les ressources
         */
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpSession httpSession = httpReq.getSession(false);

        if (httpSession != null) {

            /*
             * si on a déja fait le lock dans la minute precedente, inutile de le refaire. cela permet d'éviter
             * plusieurs mise à jours à la suite si plusieurs request en simultané comme par exemple au chargement d'une
             * demande côté front, on appele session, dateouverture, demande et d'autres servlet en parallele. On se
             * retrouve donc avec plusieurs threads liés à la même session. On ajoutera cette minute au timestamp de
             * lock.
             * 
             */
            Long intervalSinceLastUpdate = Instant.now().toEpochMilli() - httpSession.getLastAccessedTime();

            if (intervalSinceLastUpdate > 60000L) {
                Integer modificationDemandeId = (Integer) httpSession
                        .getAttribute(SessionConstant.SESSION_MODIFICATION_DEMANDE_ID);
                Integer modificationDemandeUsagerId = (Integer) httpSession
                        .getAttribute(SessionConstant.SESSION_MODIFICATION_USAGER_ID);

                if (modificationDemandeId != null && modificationDemandeUsagerId != null) {
                    AfApiClient afApiClient = AppFactoryServletUtils.getAfApiClient();
                    Long timestampValue = Instant.now().toEpochMilli() + (httpSession.getMaxInactiveInterval() * 1000L)
                            + 60000L;
                    afApiClient.lockDemande(modificationDemandeId, modificationDemandeUsagerId, timestampValue);
                    LOGGER.info("LockDemandeFilter: Verrouillage demande {} prolongé jusqu'a {}", modificationDemandeId,
                            timestampValue);
                }
            }

        }

        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {
        /* Aucun traitement particulier */
    }
}
