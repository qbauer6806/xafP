package mc.gouv.xaf.servlet;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.util.GichkeyService;
import mc.gouv.xaf.shared.dto.DonneesExternesDTO;

/**
 * Servlet permettant de gérer les sessions des usagers.
 *
 * @author qdeme
 */
public class SessionsServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7833206552171322810L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionsServlet.class);
    private static final String LOGIN = "login";

    private static final String MCONNECT_PARAM_GIVENNAME = "GivenName";
    private static final String MCONNECT_PARAM_FAMILYNAME = "FamilyName";
    private static final String MCONNECT_PARAM_BIRTHDATE = "BirthDatetime";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        LOGGER.info("====================== /sessions doGet()");
        try {

            // On tente de récupérer une session existante sans en créer une
            HttpSession session = request.getSession(false);
            LOGGER.info("SESSION : {}", session);
            if (session == null) {
                // Pas de session trouvée
                LOGGER.info("Aucune session trouvée");
                response.setStatus(HttpStatus.SC_NOT_FOUND);
                return;
            }

            // https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
            // Ajout du cookie XSRF-TOKEN

            String xsrfValue = (String) session.getAttribute(AppFactoryServletUtils.XSRF_SESSION_ATTRIBUTE);
            if (StringUtils.isBlank(xsrfValue)) {
                LOGGER.info("Aucun cookie xsrf trouvé en session");
                response.setStatus(HttpStatus.SC_NOT_FOUND);
                return;
            }
            Cookie xsrfCookie = new Cookie(AppFactoryServletUtils.XSRF_COOKIE,
                    session.getAttribute(AppFactoryServletUtils.XSRF_SESSION_ATTRIBUTE).toString());
            xsrfCookie.setSecure(true);
            xsrfCookie.setHttpOnly(true);
            response.addCookie(xsrfCookie);

            // Récupération de l'objet attaché à la session
            UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) session.getAttribute(LOGIN);
            LOGGER.info("usagerInfosDTO : {}", usagerInfosDTO);
            Map<String, String> data = new HashMap<>();

            if (usagerInfosDTO.ismConnect()) {
                JsonNode usagerJson = usagerInfosDTO.getDonneesExternes();
                ObjectMapper omapper = new ObjectMapper();
                omapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                DonneesExternesDTO donneesMConnectDTO = omapper.treeToValue(usagerJson, DonneesExternesDTO.class);
                data.put(MCONNECT_PARAM_FAMILYNAME, donneesMConnectDTO.getMconnect().getFamilyName());
                data.put(MCONNECT_PARAM_GIVENNAME, donneesMConnectDTO.getMconnect().getGivenName());
                data.put(MCONNECT_PARAM_BIRTHDATE, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                        .format(donneesMConnectDTO.getMconnect().getBirthDatetime()));
            }
            // refresh donneesexterne
            JsonNode candifp = getAfApiClient().getDonneesExternes(usagerInfosDTO.getId(), data);
            if (candifp != null && candifp.fields() != null) {
                JsonNode donneesExternes = usagerInfosDTO.getDonneesExternes();
                if (donneesExternes == null) {
                    ObjectMapper mapper = new ObjectMapper();
                    donneesExternes = mapper.createObjectNode();
                }

                Iterator<Entry<String, JsonNode>> it = candifp.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    ((ObjectNode) donneesExternes).put(entry.getKey(), entry.getValue());
                }
                usagerInfosDTO.setDonneesExternes(donneesExternes);

            }

            // Retour au client
            response.setContentType(MediaType.APPLICATION_JSON);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
            mapper.writeValue(response.getOutputStream(), usagerInfosDTO);
            response.getOutputStream().flush();
        } catch (Exception e) {
            LOGGER.error("SessionsServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            int codeErreur = getCodeErreur(e);
            response.setStatus(codeErreur);
        }

        LOGGER.info("====================== Fin /sessions doGet()");
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /sessions doPut()");

        try {
            // On tente de récupérer une session existante sans en créer une
            HttpSession session = request.getSession(false);
            LOGGER.info("SESSION : {}", session);
            if (session == null) {
                // Pas de session trouvée
                LOGGER.info("Aucune session trouvée");
                response.setStatus(HttpStatus.SC_NOT_FOUND);
                return;
            }
            // Récupération de l'objet attaché à la session
            UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) session.getAttribute(LOGIN);
            LOGGER.info("usagerInfosDTO : {}, userId={}, accessId={}", usagerInfosDTO, usagerInfosDTO.getId(),
                    usagerInfosDTO.getAccessId());

            // On ne met pas à jour s'il s'agit d'un usager courrier
            if (AppFactoryServletUtils.isUsagerCourrier(usagerInfosDTO.getId())) {
                LOGGER.info("On ne met pas à jour s'il s'agit d'un usager courrier");
                return;
            }
            usagerInfosDTO = GichkeyService.checkTokens(usagerInfosDTO, true);

            if (usagerInfosDTO != null) {

                // Stockage de cet objet d'infos d'usager dans la session HTTP
                session = request.getSession();

                session.setAttribute(LOGIN, usagerInfosDTO);
                // https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
                session.setAttribute(AppFactoryServletUtils.XSRF_SESSION_ATTRIBUTE,
                        AppFactoryServletUtils.createXsrfToken(session));
            }
        } catch (Exception e) {
            LOGGER.error("SessionsServlet - Une erreur est survenue lors de l'appel à la méthode PUT", e);
            int codeStatut = getCodeErreur(e);
            response.setStatus(codeStatut);
        }

        LOGGER.info("====================== Fin /sessions doPut()");
    }
}
