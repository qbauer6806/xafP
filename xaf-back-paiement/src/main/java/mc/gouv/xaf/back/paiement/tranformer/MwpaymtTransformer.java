package mc.gouv.xaf.back.paiement.tranformer;

import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common.TransactionInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common.UserInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit.DebitInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit.DebitOutputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.enums.ActionEnum;
import mc.gouv.xaf.back.paiement.data.entity.InformationFacturationBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.dto.DebitDTO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.enums.StatutDebitEnum;
import mc.gouv.xaf.back.service.utils.CiviliteUtilisateurs;
import mc.gouv.xaf.shared.enums.TitreUsagerEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MwpaymtTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MwpaymtTransformer.class);

    public DebitInputDTO infoDebitToMwpaymtDebitDTO(String idTs, String orderIdResid, MoyenPaiementBO moyenPaiement, InformationFacturationBO infoFacturation) {
        DebitInputDTO mwpaymtDebitDTO = new DebitInputDTO();

        // User information (ie les info de facturation)
        // TODO faire un UserInformationTransformer
        UserInformationDTO userInformation = new UserInformationDTO();
        //userInformation.setSub(null);
        userInformation.setAddress1(infoFacturation.getAdresseLigne1());
        userInformation.setCategory(infoFacturation.getRaisonSociale() != null ? "COMPANY" : "PRIVATE");
        userInformation.setCity(infoFacturation.getVille());
        userInformation.setCountry(infoFacturation.getPays());
        userInformation.setEmail(infoFacturation.getEmail());
        userInformation.setFirstName(infoFacturation.getPrenom());
        userInformation.setLastName(infoFacturation.getNom());
        userInformation.setLegalName(infoFacturation.getRaisonSociale());
        userInformation.setTitle(
                TitreUsagerEnum.valueOf("TITRE_" + infoFacturation.getCivilite().toString()).getLibelle() != null
                        ? TitreUsagerEnum.valueOf("TITRE_" + infoFacturation.getCivilite().toString()).getLibelle()
                        : "");
        userInformation.setZipCode(infoFacturation.getCodePostal());
        userInformation.setLanguage(infoFacturation.getLangue() != null ? infoFacturation.getLangue() : "FR");
        mwpaymtDebitDTO.setUserInformation(userInformation);

        mwpaymtDebitDTO.setPaymentMethodToken(moyenPaiement.getPaymentMethodToken());

        // Transaction information
        TransactionInformationDTO transactionInformation = new TransactionInformationDTO();
        transactionInformation.setMetadatakey("Numéro de demande RESID");
        transactionInformation.setMetadatavalue(orderIdResid);
        transactionInformation.setOrderId(idTs);
        transactionInformation.setAmount((float) moyenPaiement.getCommande().getMontantRestant());
        mwpaymtDebitDTO.setTransactionInformation(transactionInformation);

        return mwpaymtDebitDTO;
    }

    public DebitDTO debitOutputDTOToDebitDTO(DebitOutputDTO output) {
        DebitDTO debitDTO = new DebitDTO();
        debitDTO.setStatut(output.getTransactionAction().getActionDebit().name().equals("SUCCESS") ? StatutDebitEnum.PAID : StatutDebitEnum.UNPAID);
        debitDTO.setExpectedCaptureDate(output.getTransactionAction().getDateDebit());
        return debitDTO;
    }

}
