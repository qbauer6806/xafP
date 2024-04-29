package mc.gouv.xaf.front.controller;

import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.MotifDTO;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Servlet mettant à disposition le service /motifs avec uniquement la méthode GET pour le front.
 * Cette servlet récupère le DemarcheID et appelle le WS dans le back-end générique.
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/motifs")
public class MotifsController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MotifsController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @GetMapping
    public ResponseEntity<List<MotifDTO>> doGet(HttpServletRequest request) {
        LOGGER.info("====================== /motifs doGet()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        try {
            LOGGER.info("Appel de la démarche afin de récupérer les motifs...");
            List<MotifDTO> motifs = getAfApiClient().getMotifs();
            LOGGER.info("====================== Fin /motifs doGet()");

            return ResponseEntity.ok(motifs);
        } catch (Exception e) {
            LOGGER.error("MotifsServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.status(getCodeErreur(e)).build();
        }
    }
}
