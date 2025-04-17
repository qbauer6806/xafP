package mc.gouv.xaf.front.paiement;

import jakarta.servlet.http.HttpServletRequest;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.InfoOutputDTO;
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
        // TODO Récupération des alias dans mon guichet
        //List<PaymentMethodReferenceDTO> references = getPaiementApiClient().getReferences(usagerInfosDTO.getTokenInfo().getAccessToken());

        List<PaymentMethodReferenceDTO> monGuichetAliases = new ArrayList<>();
        PaymentMethodReferenceDTO reference1 = new PaymentMethodReferenceDTO();
        reference1.setPaymentMethodToken("5c0b802bc43d4ffe86c6492c2f08824e");
        reference1.setPaymentMethodName("Carte DSP 1");
        reference1.setPaymentMethodType("CARD");
        monGuichetAliases.add(reference1);
        PaymentMethodReferenceDTO reference2 = new PaymentMethodReferenceDTO();
        reference2.setPaymentMethodToken("c1a7c77151b74756b8fe29905f37330c");
        reference2.setPaymentMethodName("Carte DSP 2");
        reference2.setPaymentMethodType("CARD");
        monGuichetAliases.add(reference2);
        PaymentMethodReferenceDTO reference3 = new PaymentMethodReferenceDTO();
        reference3.setPaymentMethodToken("9bb1ab1c3e8f4944b87748dc26d751aa");
        reference3.setPaymentMethodName("Carte DT 1");
        reference3.setPaymentMethodType("CARD");
        monGuichetAliases.add(reference3);
        PaymentMethodReferenceDTO reference4 = new PaymentMethodReferenceDTO();
        reference4.setPaymentMethodToken("b672520d400a45d4bec74903c42bca75");
        reference4.setPaymentMethodName("Carte DT 2");
        reference4.setPaymentMethodType("CARD");
        monGuichetAliases.add(reference4);

        // Appel à lyra pour obtenir les infos
        List<MoyenPaiementOutputDTO> moyenPaiementOutputDTOs = new ArrayList<>();
        for (PaymentMethodReferenceDTO monGuichetAlias : monGuichetAliases) {
            // Appel au PSP via l'API serveur pour récupérer les infos de paiement données par monguichet
            InfoOutputDTO info = getPaiementApiClient().getMoyenPaiement(
                    mwpaymntService.getInfoInput(monGuichetAlias), usagerInfosDTO.getTokenInfo().getAccessToken());

            if (info.getPaymentMethodInformation().getPaymentMethodStatus().equals(PaymentMethodStatusEnum.ACTIVE)) {
                moyenPaiementOutputDTOs.add(
                        mwpaymntService.mwpaymentResponseToMoyenPaiement(info, monGuichetAlias.getPaymentMethodName()));
            }
        }
        moyenPaiementOutputDTOs.sort(new MoyenPaiementComparator());
        LOGGER.info("====================== /moyen-paiement GET end...");
        return ResponseEntity.ok(moyenPaiementOutputDTOs);
    }

    @PutMapping(value = { "/moyen-paiement" })
    public ResponseEntity saveMoyenPaiement(@RequestBody MoyenPaiementInputDTO moyenPaiementInput, HttpServletRequest request) {
        LOGGER.info("====================== /moyen-paiement POST start...");
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        getPaiementApiClient().updateMoyenPaiement(moyenPaiementInput);
        LOGGER.info("====================== /moyen-paiement POST end...");
        return ResponseEntity.ok().build();
    }

    class MoyenPaiementComparator implements Comparator<MoyenPaiementOutputDTO> {
        @Override
        public int compare(MoyenPaiementOutputDTO a, MoyenPaiementOutputDTO b) {
            String nomA = a.getNom();
            String nomB = b.getNom();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
            YearMonth dateA = YearMonth.parse(a.getExpiration(), formatter);
            YearMonth dateB = YearMonth.parse(b.getExpiration(), formatter);
            // crée si les nom sont identiques
            return !nomA.equals(nomB) ? nomA.compareTo(nomB) : dateA.compareTo(dateB);
        }
    }
}
