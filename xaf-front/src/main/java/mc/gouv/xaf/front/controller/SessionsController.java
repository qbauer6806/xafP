package mc.gouv.candifp.frontserver.movetoxaf.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.candifp.frontserver.movetoxaf.dto.UsagerInfosDTO;
import mc.gouv.candifp.frontserver.movetoxaf.util.GichkeyService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Servlet permettant de gérer les sessions des usagers.
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/sessions")
public class SessionsController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionsController.class);
    private static final String LOGIN = "login";

    @Autowired
    private GichkeyService gichkeyService;

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @GetMapping
    public ResponseEntity<UsagerInfosDTO> doGet(HttpServletRequest request, HttpServletResponse response) {

        LOGGER.info("====================== /sessions doGet()");
        try {

            // On tente de récupérer une session existante sans en créer une
            HttpSession session = request.getSession(false);
            LOGGER.info("SESSION : {}", session);
            if (session == null) {
                // Pas de session trouvée
                LOGGER.info("Aucune session trouvée");
                return ResponseEntity.notFound().build();
            }

            //https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
            //Ajout du cookie XSRF-TOKEN

            String xsrfValue = (String) session.getAttribute(xafFrontserverUtils.XSRF_SESSION_ATTRIBUTE);
            if (StringUtils.isBlank(xsrfValue)) {
                LOGGER.info("Aucun cookie xsrf trouvé en session");
                return ResponseEntity.notFound().build();
            }
            Cookie xsrfCookie = new Cookie(xafFrontserverUtils.XSRF_COOKIE,
                    session.getAttribute(xafFrontserverUtils.XSRF_SESSION_ATTRIBUTE).toString());
            xsrfCookie.setSecure(true);
            xsrfCookie.setHttpOnly(true);
            response.addCookie(xsrfCookie);

            // Récupération de l'objet attaché à la session
            UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) session.getAttribute(LOGIN);
            LOGGER.info("usagerInfosDTO : {}", usagerInfosDTO);
            // refresh donneesexterne
            JsonNode tsName = getAfApiClient().getDonneesExternes(usagerInfosDTO.getId());
            if (tsName != null && tsName.fields() != null && tsName.fields().hasNext()) {
                JsonNode donneesExternes = usagerInfosDTO.getDonneesExternes();
                if (donneesExternes == null) {
                    ObjectMapper mapper = new ObjectMapper();
                    donneesExternes = mapper.createObjectNode();
                }
                Map.Entry<String, JsonNode> entry = tsName.fields().next();
                ((ObjectNode) donneesExternes).put(entry.getKey(), entry.getValue());
                usagerInfosDTO.setDonneesExternes(donneesExternes);
            }

            LOGGER.info("====================== Fin /sessions doGet()");

            // Retour au client
            return ResponseEntity.ok(usagerInfosDTO);
        } catch (Exception e) {
            LOGGER.error("SessionsServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.status(getCodeErreur(e)).build();
        }
    }

    @PutMapping
    public ResponseEntity doPut(HttpServletRequest request) {
        LOGGER.info("====================== /sessions doPut()");

        try {
            // On tente de récupérer une session existante sans en créer une
            HttpSession session = request.getSession(false);
            LOGGER.info("SESSION : {}", session);
            if (session == null) {
                // Pas de session trouvée
                LOGGER.info("Aucune session trouvée");
                return ResponseEntity.notFound().build();
            }
            // Récupération de l'objet attaché à la session
            UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) session.getAttribute(LOGIN);
            LOGGER.info("usagerInfosDTO : {}, userId={}, accessId={}",
                    usagerInfosDTO, usagerInfosDTO.getId(), usagerInfosDTO.getAccessId());

            // On ne met pas à jour s'il s'agit d'un usager courrier
            if (xafFrontserverUtils.isUsagerCourrier(usagerInfosDTO.getId())) {
                LOGGER.info("On ne met pas à jour s'il s'agit d'un usager courrier");
                return ResponseEntity.ok().build();
            }
            usagerInfosDTO = gichkeyService.checkTokens(usagerInfosDTO, true);

            if (usagerInfosDTO != null) {

                // Stockage de cet objet d'infos d'usager dans la session HTTP
                session = request.getSession();

                session.setAttribute(LOGIN, usagerInfosDTO);
                // https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
                session.setAttribute(xafFrontserverUtils.XSRF_SESSION_ATTRIBUTE,
                        xafFrontserverUtils.createXsrfToken(session));
            }
            LOGGER.info("====================== Fin /sessions doPut()");

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            LOGGER.error("SessionsServlet - Une erreur est survenue lors de l'appel à la méthode PUT", e);
            return ResponseEntity.status(getCodeErreur(e)).build();
        }
    }
}
