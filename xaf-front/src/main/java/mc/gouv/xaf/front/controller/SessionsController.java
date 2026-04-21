package mc.gouv.xaf.front.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.GichkeyService;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Servlet permettant de gérer les sessions des usagers.
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionsController.class);
    private static final String LOGIN = "login";

    private final GichkeyService gichkeyService;
    private final XafFrontserverUtils xafFrontserverUtils;

    @GetMapping
    public ResponseEntity<UsagerInfosDTO> doGet(HttpServletRequest request, HttpServletResponse response) {

        LOGGER.info("====================== /sessions doGet()");
        try {

            // On tente de récupérer une session existante sans en créer une
            HttpSession session = request.getSession(false);
            LOGGER.debug("SESSION : {}", session);
            if (session == null) {
                // Pas de session trouvée
                LOGGER.info("Aucune session trouvée");
                return ResponseEntity.notFound().build();
            }

            //https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
            //Ajout du cookie XSRF-TOKEN

            String xsrfValue = (String) session.getAttribute(XafFrontserverUtils.XSRF_SESSION_ATTRIBUTE);
            if (StringUtils.isBlank(xsrfValue)) {
                LOGGER.info("Aucun cookie xsrf trouvé en session");
                return ResponseEntity.notFound().build();
            }
            Cookie xsrfCookie = new Cookie(XafFrontserverUtils.XSRF_COOKIE,
                    session.getAttribute(XafFrontserverUtils.XSRF_SESSION_ATTRIBUTE).toString());
            xsrfCookie.setSecure(true);
            xsrfCookie.setHttpOnly(true);
            response.addCookie(xsrfCookie);

            // Récupération de l'objet attaché à la session
            UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) session.getAttribute(LOGIN);
            LOGGER.debug("usagerInfosDTO : {}", usagerInfosDTO);

            LOGGER.info("====================== Fin /sessions doGet()");

            // Retour au client
            return ResponseEntity.ok(usagerInfosDTO);
        } catch (Exception e) {
            LOGGER.error("SessionsServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.status(xafFrontserverUtils.getCodeErreur(e)).build();
        }
    }

    @PutMapping
    public ResponseEntity doPut(HttpServletRequest request) {
        LOGGER.info("====================== /sessions doPut()");

        try {
            // On tente de récupérer une session existante sans en créer une
            HttpSession session = request.getSession(false);
            LOGGER.debug("SESSION : {}", session);
            if (session == null) {
                // Pas de session trouvée
                LOGGER.info("Aucune session trouvée");
                return ResponseEntity.notFound().build();
            }
            // Récupération de l'objet attaché à la session
            UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) session.getAttribute(LOGIN);
            LOGGER.debug("usagerInfosDTO : {}, userId={}, accessId={}", usagerInfosDTO, usagerInfosDTO.getId(),
                    usagerInfosDTO.getAccessId());

            // On ne met pas à jour s'il s'agit d'un usager courrier
            if (XafFrontserverUtils.isUsagerCourrier(usagerInfosDTO.getId())) {
                LOGGER.debug("On ne met pas à jour s'il s'agit d'un usager courrier");
                return ResponseEntity.ok().build();
            }
            usagerInfosDTO = gichkeyService.checkTokens(usagerInfosDTO, true);

            if (usagerInfosDTO != null) {

                // Stockage de cet objet d'infos d'usager dans la session HTTP
                session = request.getSession();

                session.setAttribute(LOGIN, usagerInfosDTO);
                // https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
                session.setAttribute(XafFrontserverUtils.XSRF_SESSION_ATTRIBUTE,
                        XafFrontserverUtils.createXsrfToken(session));
            }
            LOGGER.info("====================== Fin /sessions doPut()");

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            LOGGER.error("SessionsServlet - Une erreur est survenue lors de l'appel à la méthode PUT", e);
            return ResponseEntity.status(xafFrontserverUtils.getCodeErreur(e)).build();
        }
    }
}
