package mc.gouv.xaf.front.paiement;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.paiement.enums.TableauPaiementTypeEnum;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.paiement.tableaupaiement.TableauDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@RequiredArgsConstructor
public class TableauPaiementController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TableauPaiementController.class);

    private final XafFrontserverUtils xafFrontserverUtils;

    @GetMapping(value = { "/tableau-paiement" })
    public ResponseEntity<List<TableauDTO>> getTableauPaiement(@RequestParam TableauPaiementTypeEnum type, @RequestParam String ids,
            HttpServletRequest request) {
        LOGGER.info("====================== /tableau-paiement GET start...");

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // On va chercher selon les paramètres donnés (ie dans les brouillons ou dans les
        // demandes) le montant de l'opération ainsi que le total à fournir au front
        List<TableauDTO> tableauPaiementResponse = xafFrontserverUtils.getPaiementApiClient().getTableauPaiement(ids,
                type.name(), usagerInfosDTO.getId());

        LOGGER.info("====================== /tableau-paiement GET end...");
        return ResponseEntity.ok(tableauPaiementResponse);
    }
}
