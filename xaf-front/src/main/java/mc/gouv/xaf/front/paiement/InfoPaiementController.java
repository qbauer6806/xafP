package mc.gouv.xaf.front.paiement;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.apiclient.paiement.PaiementApiClient;
import mc.gouv.xaf.apiclient.paiement.monetico.dto.MoneticoDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterOutputDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.MwpaymntService;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.paiement.enums.PSPEnum;
import mc.gouv.xaf.shared.paiement.infopaiement.InfoPaiementInputDTO;
import mc.gouv.xaf.shared.paiement.infopaiement.InfoPaiementOutputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@RequiredArgsConstructor
public class InfoPaiementController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfoPaiementController.class);

    private final XafFrontserverUtils xafFrontserverUtils;
    private final MwpaymntService mwpaymntService;

    @PostMapping(value = { "/info-paiement" })
    public ResponseEntity infoPaiement(@RequestBody InfoPaiementInputDTO infoPaiementInput,
            HttpServletRequest request) {
        LOGGER.info("====================== /info-paiement POST start...");

        // Appel à la gateway de paiement pour récupérer le formToken
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LOGGER.info("Récupération du PSP...");

        // On garde cette condition car mwpaymnt ne gère pas encore monetico
        if (infoPaiementInput.getProviderName().equalsIgnoreCase(PSPEnum.MONETICO.name())) {
            LOGGER.info("====================== Fin /info-paiement doPost() monetico\n");
            return processMoneticoCall(usagerInfosDTO, infoPaiementInput);
        } else if (infoPaiementInput.getProviderName().equalsIgnoreCase(PSPEnum.LYRA.name())) {
            return processMwpaymntCall(usagerInfosDTO, infoPaiementInput);
        }
        return ResponseEntity.ok().build();
    }

    private ResponseEntity processMwpaymntCall(UsagerInfosDTO usagerInfosDTO, InfoPaiementInputDTO infoPaiementInput) {
        RegisterInputDTO registerInput = mwpaymntService.getRegisterInput(usagerInfosDTO);
        PaiementApiClient paiementApiClient = xafFrontserverUtils.getPaiementApiClient();

        // Appel au PSP via l'API server pour générer le form token
        String accessToken = usagerInfosDTO.getTokenInfo().getAccessToken();
        RegisterOutputDTO token = paiementApiClient.postInfoPaiement(registerInput, accessToken);
        String orderId = registerInput.getTransactionInformation().getOrderId();

        // Création du moyen de paiement en base de donnée
        boolean moyenPaiementCree = paiementApiClient.createMoyenPaiement(infoPaiementInput.getDemandesId(), usagerInfosDTO,
                orderId, accessToken, infoPaiementInput.getRaisonSociale(), infoPaiementInput.getLangue());
        InfoPaiementOutputDTO infoPaiementOutputDTO = mwpaymntService.mwpaymtRegisterResponseToInfoPaiementOutputDTO(token);

        LOGGER.info("====================== /info-paiement POST end...");

        if (moyenPaiementCree) {
            return ResponseEntity.ok(infoPaiementOutputDTO);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    private ResponseEntity processMoneticoCall(UsagerInfosDTO usagerInfosDTO, InfoPaiementInputDTO infoPaiementInput) {
        String demandeIds = infoPaiementInput.getDemandesId();
        String langue = infoPaiementInput.getLangue();
        boolean iframe = infoPaiementInput.isIframe();
        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();
        LOGGER.info("Récupération des données de paiement pour les demandes {}...", demandeIds);
        MoneticoDTO paiement = xafFrontserverUtils.getMoneticoApiClient().getPaiement(demandeIds, langue, usagerId, iframe);
        LOGGER.info("====================== Fin /info-paiement doPost() Monetico\n");
        return ResponseEntity.ok(paiement);
    }
}
