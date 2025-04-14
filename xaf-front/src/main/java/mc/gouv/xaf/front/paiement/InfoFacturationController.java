package mc.gouv.xaf.front.paiement;

import jakarta.servlet.http.HttpServletRequest;
import mc.gouv.xaf.front.controller.AbstractXafController;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.paiement.infofacturation.InfoFacturationResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class InfoFacturationController  extends AbstractXafController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfoFacturationController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @GetMapping(value = { "/info-facturation" })
    public ResponseEntity getInfoFacturation(HttpServletRequest request) {
        LOGGER.info("====================== /info-facturation GET start...");

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        InfoFacturationResponseDTO infoFacturation = getPaiementApiClient().getInfoFacturation(usagerInfosDTO.getId());
        LOGGER.info("====================== /info-facturation GET end...");
        return ResponseEntity.ok(infoFacturation);
    }

    @PostMapping(value = { "/info-facturation" })
    public ResponseEntity saveInfoFacturation(HttpServletRequest request) {
        // TODO
        LOGGER.info("====================== /info-facturation POST start...");

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LOGGER.info("====================== /info-facturation POST end...");
        return ResponseEntity.ok().build();
    }
}
