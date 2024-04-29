package mc.gouv.xaf.front.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Servlet mettant à disposition le service /periodesouverture avec uniquement la méthode GET pour le front.
 * Cette servlet récupère le DemarcheID et appelle le WS dans le back-end générique.
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/periodesouverture")
public class PeriodesOuvertureController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodesOuvertureController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @GetMapping
    public ResponseEntity<String> doGet(HttpServletRequest request) {
        LOGGER.info("====================== /periodesouverture doGet()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        LOGGER.info("Appel de la démarche afin de récupérer les périodes d'ouverture...");
        List<PeriodeOuvertureDTO> periodes = getAfApiClient().getPeriodesOuverture();
        // on utilise jackson pour transformer en string afin d'avoir les dates au format attendu par wysi (1712354340000 au lieu de 2024-...)
        ObjectMapper mapper = new ObjectMapper();
        String periodesString;
        try {
            periodesString = mapper.writeValueAsString(periodes);
        } catch (JsonProcessingException e) {
            LOGGER.error("PeriodesOuvertureController - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.internalServerError().build();
        }
        LOGGER.info("====================== Fin /periodesouverture doGet()");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(periodesString);
    }
}
