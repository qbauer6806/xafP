package mc.gouv.xaf.front.paiement;

import jakarta.servlet.http.HttpServletRequest;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.PaymentMethodInformationDTO;
import mc.gouv.xaf.front.controller.AbstractXafController;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.MwpaymntService;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.paiement.enums.PaymentMethodStatusEnum;
import mc.gouv.xaf.shared.paiement.mongichet.PaymentMethodReferenceDTO;
import mc.gouv.xaf.shared.paiement.moyenpaiement.MoyenPaiementInputDTO;
import mc.gouv.xaf.shared.paiement.moyenpaiement.MoyenPaiementOutputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class MoyenPaiementController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoyenPaiementController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private MwpaymntService mwpaymntService;

    @GetMapping(value = { "/moyen-paiement" })
    public ResponseEntity getMoyenPaiement(HttpServletRequest request) {
        LOGGER.info("====================== /moyen-paiement GET start...");

        // Appel à la gateway de paiement pour récupérer le formToken
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PaymentMethodReferenceDTO> references = getPaiementApiClient().getReferences(
                usagerInfosDTO.getTokenInfo().getAccessToken());
        references.sort(new PaymentMethodReferenceComparator());

        // Appel à lyra pour obtenir les infos
        List<MoyenPaiementOutputDTO> moyenPaiementOutputDTOs = new ArrayList<>();
        for (PaymentMethodReferenceDTO monGuichetAlias : references) {
            try {
                // Appel au PSP via l'API serveur pour récupérer les infos de paiement données par monguichet
                PaymentMethodInformationDTO pmi = getPaiementApiClient().getMoyenPaiement(
                        mwpaymntService.getInfoInput(monGuichetAlias), usagerInfosDTO.getTokenInfo().getAccessToken());

                if (pmi.getPaymentMethodStatus().equals(PaymentMethodStatusEnum.ACTIVE)) {
                    moyenPaiementOutputDTOs.add(mwpaymntService.mwpaymentResponseToMoyenPaiement(pmi,
                            monGuichetAlias.getPaymentMethodName()));
                }
            } catch (Exception e) {
                // Log l'erreur mais continue la boucle
                LOGGER.warn("Impossible de récupérer le moyen de paiement pour {} : {}",
                        monGuichetAlias.getPaymentMethodName(), e.getMessage());
            }
        }
        LOGGER.info("====================== /moyen-paiement GET end...");
        return ResponseEntity.ok(moyenPaiementOutputDTOs);
    }

    @PutMapping(value = { "/moyen-paiement" })
    public ResponseEntity saveMoyenPaiement(@RequestBody MoyenPaiementInputDTO moyenPaiementInput, HttpServletRequest request) {
        LOGGER.info("====================== /moyen-paiement PUT start...");
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        getPaiementApiClient().updateMoyenPaiement(moyenPaiementInput, usagerInfosDTO.getTokenInfo().getAccessToken());
        LOGGER.info("====================== /moyen-paiement PUT end...");
        return ResponseEntity.ok().build();
    }

    class PaymentMethodReferenceComparator implements Comparator<PaymentMethodReferenceDTO> {
        @Override
        public int compare(PaymentMethodReferenceDTO a, PaymentMethodReferenceDTO b) {
            String nomA = a.getPaymentMethodName();
            String nomB = b.getPaymentMethodName();
            // Compare d'abord les noms
            int comparaisonNom = nomA.compareTo(nomB);
            if (comparaisonNom != 0) {
                return comparaisonNom;
            }

            // Si les noms sont identiques, compare les dates de création
            LocalDateTime dateA = a.getCreatedAt();
            LocalDateTime dateB = b.getCreatedAt();

            // Ici dateB.compareTo(dateA) → tri décroissant (plus récent d'abord)
            return dateB.compareTo(dateA);
        }
    }
}
