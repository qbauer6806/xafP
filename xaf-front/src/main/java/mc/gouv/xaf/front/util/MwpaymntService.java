package mc.gouv.xaf.front.util;

import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common.TransactionInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common.UserInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.InfoCancelInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.InfoOutputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.PaymentMethodInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.enums.ActionEnum;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.enums.CompanyEnum;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.enums.PaymentMethodStatusEnum;
import mc.gouv.xaf.front.dto.PaymentMethodReferenceDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.shared.paiement.moyenpaiement.MoyenPaiementOutputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MwpaymntService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MwpaymntService.class);


    public RegisterInputDTO getRegisterInput(UsagerInfosDTO usagerInfosDTO) {
        RegisterInputDTO registerInputDTO = new RegisterInputDTO();
        registerInputDTO.setAction(ActionEnum.REGISTER.name());
        registerInputDTO.setCompany(CompanyEnum.DSP.name());

        // Payment method information
        PaymentMethodInformationDTO information = new PaymentMethodInformationDTO();
        information.setPaymentMethodType("card");
        information.setThreeDs("challenge_mandate");
        registerInputDTO.setPaymentMethodInformation(information);

        // Transaction Information
        TransactionInformationDTO transactionInformation = new TransactionInformationDTO();
        transactionInformation.setCurrency("EUR");
        transactionInformation.setMetadatakey("Téléservice");
        // TODO réfléchir à l'avoir en paramètre pour identifier l'appelant
        transactionInformation.setMetadatavalue("RESCART");
        transactionInformation.setOrderId("TestOrderId");
        registerInputDTO.setTransactionInformation(transactionInformation);

        // Transaction ID
        registerInputDTO.setTransactionId("TestTransactionId");

        // User information
        UserInformationDTO userInformation = new UserInformationDTO();
        userInformation.setAddress1(usagerInfosDTO.getAdresse1());
        userInformation.setAddress2(usagerInfosDTO.getAdresse2());
        userInformation.setCategory("PRIVATE");
        userInformation.setCity(usagerInfosDTO.getVille());
        userInformation.setCountry(usagerInfosDTO.getPaysCode());
        userInformation.setEmail(usagerInfosDTO.getEmail());
        userInformation.setFirstName(usagerInfosDTO.getPrenom());
        userInformation.setLastName(usagerInfosDTO.getNom());
        userInformation.setLegalName(usagerInfosDTO.getNom());
        userInformation.setTitle(usagerInfosDTO.getTitreLabel());
        userInformation.setZipCode(usagerInfosDTO.getCodePostal());
        userInformation.setLanguage("FR");
        registerInputDTO.setUserInformation(userInformation);
        return registerInputDTO;
    }

    public InfoCancelInputDTO getInfoInput(PaymentMethodReferenceDTO reference) {
        InfoCancelInputDTO result = new InfoCancelInputDTO();
        // TODO a changer pour la company réelle
        result.setCompany(CompanyEnum.DSP.name());
        // TODO à réfléchir
        result.setTransactionId("tetTransactionID");
        mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.PaymentMethodInformationDTO paymentMethodInformationDTO = new mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.PaymentMethodInformationDTO();
        paymentMethodInformationDTO.setPaymentMethodToken(reference.getPaymentMethodToken());
        paymentMethodInformationDTO.setPaymentMethodType(reference.getPaymentMethodType());
        result.setPaymentMethodInformation(paymentMethodInformationDTO);
        return result;
    }

    public MoyenPaiementOutputDTO mwpaymentResponseToMoyenPaiement(InfoOutputDTO infoOutput, String moyenPaiementName) {
        MoyenPaiementOutputDTO currentMoyenPaiement = new MoyenPaiementOutputDTO();
        mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.PaymentMethodInformationDTO paymentMethodInformation = infoOutput.getPaymentMethodInformation();
        currentMoyenPaiement.setNumero(paymentMethodInformation.getPan());
        currentMoyenPaiement.setType(paymentMethodInformation.getEffectiveBrand());
        currentMoyenPaiement.setExpiration(calculateExpiration(paymentMethodInformation.getExpiryMonth(),
                paymentMethodInformation.getExpiryYear()));
        currentMoyenPaiement.setNom(moyenPaiementName);
        // TODO allé chercher ce nom dans mon guichet
        currentMoyenPaiement.setId(paymentMethodInformation.getPaymentMethodToken());
        return currentMoyenPaiement;
    }

    private String calculateExpiration(String expiryMonth, String expiryYear) {
        StringBuilder result = new StringBuilder();
        result.append(expiryMonth).append("/").append(expiryYear);
        return result.toString();
    }
}
