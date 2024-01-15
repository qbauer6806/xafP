package mc.gouv.xaf.front.controller;

import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 * Servlet permettant de rediriger l'usager sur le Back-Office
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/redirect-to-backoffice")
public class RedirectToBackOfficeController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectToBackOfficeController.class);
    private static final String TOKEN_ID_DEMANDE = "<id>";

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    @GetMapping
    public ResponseEntity doGet(HttpServletRequest request) {
        LOGGER.info("====================== /redirect-to-backoffice doGet()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        // Action impossible pour les usagers venant d'une création de demande courrier
        if (!usagerInfosDTO.isUsagerCourrier()) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    "Utilisateur non autorisé car non usager courrier");
        }

        try {
            //redirection par default sur l'accueil car le lien abandon a été cliqué
            String urlDemande = propertiesResolver.getBackOfficeUrl();
            //dans le cas de la fin de la création
            String idDemandeStr = request.getParameter("id");
            if (StringUtils.isNotBlank(idDemandeStr)) {
                int idDemande = Integer.parseInt(idDemandeStr);
                urlDemande = propertiesResolver.getBackOfficeDemandeUrl();
                urlDemande = StringUtils.replace(urlDemande, TOKEN_ID_DEMANDE, idDemande + "");
            }
            LOGGER.info("Redirection vers : {}", urlDemande);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(urlDemande));
            LOGGER.info("====================== Fin /redirect-to-backoffice doGet()");

            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        } catch (Exception e) {
            LOGGER.error("RedirectToBackOfficeServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.status(getCodeErreur(e)).build();
        }
    }
}
