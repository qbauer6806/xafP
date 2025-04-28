package mc.gouv.xaf.back.dsp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import mc.gouv.xaf.back.dsp.dto.ResidDebitInputDTO;
import mc.gouv.xaf.back.dsp.dto.ResidStatutCaisseDTO;
import mc.gouv.xaf.back.dsp.dto.ResidTarifDTO;
import mc.gouv.xaf.back.paiement.dto.DebitDTO;
import mc.gouv.xaf.back.paiement.service.PaiementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;
import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logEndMethod;

@RestController
@RequestMapping(value = "/api/v1/paiement", produces = "application/json")
public class ResidApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResidApiController.class);

    @Autowired
    private PaiementService paiementService;

    // APIs destinées à RESID
    @Operation(
            summary = "API permettant à RESID de mettre à jour le tarif d'un certificat de résidence",
            description = "API permettant à RESID de mettre à jour le tarif d'un certificat de résidence. Un JWT généré par Keycloak doit etre fourni"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Débit effectué avec succès",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur durant l'appel",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping(value = "/tarif")
    public void majTarif(@RequestBody ResidTarifDTO input) {
        logStartMethod(LOGGER);
        paiementService.majTarif(input.getTarif());
        LOGGER.info("Fin de la mise à jour du tarif. Nouvelle valeur {}", input.getTarif());
        logEndMethod(LOGGER);
    }

    @Operation(
            summary = "API permettant à RESID de mettre à jour le statut de la caisse DSP",
            description = "API permettant à RESID de mettre à jour le statut de la caisse DSP dans le cadre d'un certificat de résidence payable en ligne. Un JWT généré par Keycloak doit etre fourni"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Débit effectué avec succès",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur durant l'appel",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping(value = "/statutCaisse")
    public void majStatutCaisse(@RequestBody ResidStatutCaisseDTO input) {
        logStartMethod(LOGGER);
        paiementService.majStatutCaisse(input.getAction().name());
        LOGGER.info("Fin de la mise à jour du statut de la caisse. La caisse est maintenant {}", input.getAction().name());
        logEndMethod(LOGGER);
    }

    @Operation(
            summary = "API permettant à RESID de déclencher un débit en fonction des informations fournies dans le body",
            description = "API permettant à RESID de déclencher un débit en fonction des informations fournies dans le body. Un JWT généré par Keycloak doit etre fourni"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Débit effectué avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DebitDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erreur durant l'appel",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/debit")
    public DebitDTO debit(@RequestBody ResidDebitInputDTO input, HttpServletRequest request) {
        logStartMethod(LOGGER);
        String authorization = request.getHeader("Authorization");
        String jwt = "";
        if (authorization != null && authorization.startsWith("Bearer ")) {
            jwt = authorization.substring(7); // Enlève "Bearer "
        }
        DebitDTO debit = paiementService.debit(input.getIdTs(), input.getOrderIdResid(), jwt);
        logEndMethod(LOGGER);
        return debit;
    }
}
