package mc.gouv.xaf.front.paiement;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.paiement.infofacturation.InfoFacturationResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class InfoFacturationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfoFacturationController.class);

    private final XafFrontserverUtils xafFrontserverUtils;

    @GetMapping(value = { "/info-facturation" })
    public ResponseEntity<InfoFacturationResponseDTO> getInfoFacturation(HttpServletRequest request) {
        LOGGER.info("====================== /info-facturation GET start...");

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        InfoFacturationResponseDTO infoFacturation = xafFrontserverUtils.getPaiementApiClient()
                .getInfoFacturation(usagerInfosDTO);
        LOGGER.info("====================== /info-facturation GET end...");
        return ResponseEntity.ok(infoFacturation);
    }
}
