package mc.gouv.xaf.front.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.PaysDTO;

/**
 * Proxy vers les nomenclatures PAY-1 et NATIO de NOMEN
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/pays")
public class PaysController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaysController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @GetMapping
    public ResponseEntity<String> doGet(@RequestParam(required = false) String locale, HttpServletRequest request) {
        LOGGER.info("====================== /pays doGet()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        try {
            List<PaysDTO> pays = getAfApiClient().getPays();
            ObjectMapper mapper = new ObjectMapper();
            String repJson = mapper.writeValueAsString(pays);
            LOGGER.info("====================== Fin /pays doGet()");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(repJson);
        } catch (Exception e) {
            LOGGER.error("PaysServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
