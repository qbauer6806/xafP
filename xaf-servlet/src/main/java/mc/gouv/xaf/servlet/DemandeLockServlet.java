package mc.gouv.xaf.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SessionConstant;
import mc.gouv.xaf.shared.SharedMessages;

/**
 * Servlet permettant de verrouiller/deverrouiller une demande en y aposant un timestamp. le timestamp correspond à la
 * durée de vie de la session.
 *
 * @author agaidi
 */
public class DemandeLockServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027084L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeLockServlet.class);

    /**
     * Vérifie si l'utilisateur est autorisé à faire la requête et prépare les objets communs aux requêtes :<br>
     * <ol>
     * <li>Un UsagerInfosDTO contenant les infos de l'usager.</li>
     * <li>L'id de la demande déjà parsé en entier (si présent)</li>
     * <li>Flag indiquant la présence de demandes complémentaires</li>
     * <li>L'id de la demande d'information complémentaire déjà parsé en entier (si présent)</li>
     * </ol>
     */
    private Object[] setup(HttpServletRequest request, HttpServletResponse response) {
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return new Object[0];
        }

        String sDemandeId = request.getParameter("demandeId");
        Integer demandeId = sDemandeId != null ? Integer.parseInt(sDemandeId) : null;

        return new Object[] { usagerInfosDTO, demandeId };
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandeLock doPut()");

        Object[] params = setup(request, response);
        if (params.length == 0) {
            return;
        }
        Integer usagerId = ((UsagerInfosDTO) params[0]).getId();
        Integer demandeId = (Integer) params[1];

        try {
            String repJson = null;
            AfApiClient afApiClient = getAfApiClient();
            ObjectMapper mapper = new ObjectMapper();

            if (demandeId != null) {
                /* gestion du lock */
                verrouillerDemande(request, afApiClient, usagerId, demandeId);
                repJson = mapper.writeValueAsString("{\"result\": \"ok\"}");
            }

            response.setContentType(MediaType.APPLICATION_JSON);
            if (repJson != null) {
                IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
            }

        } catch (IOException e) {
            LOGGER.error("DemandeLockServlet - Une erreur est survenue lors de l'appel à la méthode PUT", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        response.setStatus(HttpStatus.SC_CREATED);

        LOGGER.info("====================== Fin /demandeLock doPut()");
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandeLock doDelete()");

        Object[] params = setup(request, response);
        if (params.length == 0) {
            return;
        }
        Integer usagerId = ((UsagerInfosDTO) params[0]).getId();
        Integer demandeId = (Integer) params[1];

        try {
            String repJson = null;
            AfApiClient afApiClient = getAfApiClient();
            ObjectMapper mapper = new ObjectMapper();

            if (demandeId != null) {
                /* gestion du lock */
                deverrouillerDemande(request, afApiClient, usagerId, demandeId);
                repJson = mapper.writeValueAsString("{\"result\": \"ok\"}");
            }

            response.setContentType(MediaType.APPLICATION_JSON);
            if (repJson != null) {
                IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
            }

        } catch (IOException e) {
            LOGGER.error("DemandeLockServlet - Une erreur est survenue lors de l'appel à la méthode DELETE", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        response.setStatus(HttpStatus.SC_CREATED);

        LOGGER.info("====================== Fin /demandeLock doDelete()");
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

                LOGGER.info("DemandeLockServlet: Demande {} déverrouillée", modificationDemandeId);
            }

            /*
             * la demande sera lockée jusqu'à l'expiration de la session, cad l'instant présent + durée max d'inactivité
             * de la session plus une minute de marge.
             */
            Long timestampValue = Instant.now().toEpochMilli() + (httpSession.getMaxInactiveInterval() * 1000L)
                    + 60000L;
            /* on lock la demande */
            afApiClient.lockDemande(demandeId, usagerId, timestampValue);
            request.getSession().setAttribute(SessionConstant.SESSION_MODIFICATION_DEMANDE_ID, demandeId);
            request.getSession().setAttribute(SessionConstant.SESSION_MODIFICATION_USAGER_ID, usagerId);
        }
    }

    private void deverrouillerDemande(HttpServletRequest request, AfApiClient afApiClient, Integer usagerId,
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
                LOGGER.info("DemandeLockServlet: Demande {} déverrouillée", modificationDemandeId);
            }
            afApiClient.unlockDemande(demandeId, usagerId);
            httpSession.setAttribute(SessionConstant.SESSION_MODIFICATION_DEMANDE_ID, null);
            httpSession.setAttribute(SessionConstant.SESSION_MODIFICATION_USAGER_ID, null);
        }

    }
}
