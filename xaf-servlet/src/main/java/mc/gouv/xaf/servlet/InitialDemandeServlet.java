package mc.gouv.xaf.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SessionConstant;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DonneesExternesDTO;

/**
 * Servlet mettant à disposition les donnees externes
 *
 * @author agaidi
 */
public class InitialDemandeServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027084L;
    private static final String MCONNECT_PARAM_GIVENNAME = "GivenName";
    private static final String MCONNECT_PARAM_FAMILYNAME = "FamilyName";
    private static final String MCONNECT_PARAM_BIRTHDATE = "BirthDatetime";
    private static final Logger LOGGER = LoggerFactory.getLogger(InitialDemandeServlet.class);

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
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /InitialDemandeServlet doGet()");

        Object[] params = setup(request, response);
        if (params.length == 0) {
            return;
        }

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        if (usagerInfosDTO.ismConnect()) {

            DonneesExternesDTO donneesMConnectDTO;

            Map<String, String[]> data = new HashMap<>();

            if (usagerInfosDTO.ismConnect()) {
                JsonNode usagerJson = usagerInfosDTO.getDonneesExternes();
                ObjectMapper omapper = new ObjectMapper();
                omapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                data.put("usagerId", new String[] { usagerInfosDTO.getId() + "" });
                try {
                    donneesMConnectDTO = omapper.treeToValue(usagerJson, DonneesExternesDTO.class);
                    data.put(MCONNECT_PARAM_FAMILYNAME,
                            new String[] { donneesMConnectDTO.getMconnect().getFamilyName() });
                    data.put(MCONNECT_PARAM_GIVENNAME,
                            new String[] { donneesMConnectDTO.getMconnect().getGivenName() });
                    data.put(MCONNECT_PARAM_BIRTHDATE, new String[] { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                            .format(donneesMConnectDTO.getMconnect().getBirthDatetime()) });

                    JsonNode retour = getAfApiClient().getDonneesExternes(usagerInfosDTO.getId(), data);

                    response.setContentType(MediaType.APPLICATION_JSON);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
                    mapper.writeValue(response.getOutputStream(), retour);
                    response.setStatus(HttpStatus.SC_OK);
                    response.getOutputStream().flush();
                } catch (JsonProcessingException e) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    LOGGER.error("JsonProcessingException. Impossible de recuperer les donnees externes", e);
                } catch (IOException e) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    LOGGER.error("IOException. Impossible de recuperer les donnees externes", e);
                }

            }

        }

        LOGGER.info("====================== Fin /InitialDemandeServlet doGet()");
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

        AfApiClient afApiClient = getAfApiClient();

        if (demandeId != null) {
            /* gestion du lock */
            deverrouillerDemande(request, afApiClient, usagerId, demandeId);

        }

        response.setContentType(MediaType.APPLICATION_JSON);

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
