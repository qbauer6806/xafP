package mc.gouv.xaf.front.util;

import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common.TransactionInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common.UserInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.InfoCancelInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.PaymentMethodInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterOutputDTO;
import mc.gouv.xaf.shared.paiement.mongichet.PaymentMethodReferenceDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.shared.paiement.infopaiement.AnswerDTO;
import mc.gouv.xaf.shared.paiement.infopaiement.InfoPaiementOutputDTO;
import mc.gouv.xaf.shared.paiement.moyenpaiement.MoyenPaiementOutputDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class MwpaymntService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MwpaymntService.class);

    @Autowired
    private FrontGouvPropertiesResolver gouvPropertiesResolver;


    public RegisterInputDTO getRegisterInput(UsagerInfosDTO usagerInfosDTO) {
        LOGGER.info("Création du RegisterInputDTO utilisé pour appeler le middleware de paiement");
        RegisterInputDTO registerInputDTO = new RegisterInputDTO();
        registerInputDTO.setCallbackUri(gouvPropertiesResolver.getMwpaymtCallbackUri());

        // Transaction Information
        TransactionInformationDTO transactionInformation = new TransactionInformationDTO();
        transactionInformation.setMetadatakey("Téléservice");
        // TODO réfléchir à l'avoir en paramètre pour identifier l'appelant
        transactionInformation.setMetadatavalue("RESCART");
        transactionInformation.setOrderId(generateOrderId(gouvPropertiesResolver.getDemarcheId()));
        registerInputDTO.setTransactionInformation(transactionInformation);

        // User information
        UserInformationDTO userInformation = new UserInformationDTO();
        userInformation.setSub(usagerInfosDTO.getSub());
        userInformation.setAddress1(usagerInfosDTO.getAdresse1());
        userInformation.setCategory("PRIVATE");
        userInformation.setCity(usagerInfosDTO.getVille());
        userInformation.setCountry(usagerInfosDTO.getPaysCode());
        userInformation.setEmail(usagerInfosDTO.getEmail());
        userInformation.setFirstName(usagerInfosDTO.getPrenom());
        userInformation.setLastName(usagerInfosDTO.getNom());
        userInformation.setLegalName(usagerInfosDTO.getRaisonSociale());
        userInformation.setTitle(usagerInfosDTO.getTitreLabel());
        userInformation.setZipCode(usagerInfosDTO.getCodePostal());
        userInformation.setLanguage("FR");
        registerInputDTO.setUserInformation(userInformation);
        return registerInputDTO;
    }

    public InfoCancelInputDTO getInfoInput(PaymentMethodReferenceDTO reference) {
        InfoCancelInputDTO result = new InfoCancelInputDTO();
        result.setPaymentMethodToken(reference.getPaymentMethodToken());
        return result;
    }

    public MoyenPaiementOutputDTO mwpaymentResponseToMoyenPaiement(PaymentMethodInformationDTO pmi, String moyenPaiementName) {
        MoyenPaiementOutputDTO currentMoyenPaiement = new MoyenPaiementOutputDTO();
        currentMoyenPaiement.setNumero(pmi.getPan());
        currentMoyenPaiement.setType(pmi.getEffectiveBrand());
        currentMoyenPaiement.setExpiration(calculateExpiration(pmi.getExpiryMonth(),
                pmi.getExpiryYear()));
        currentMoyenPaiement.setNom(moyenPaiementName);
        // TODO allé chercher ce nom dans mon guichet
        currentMoyenPaiement.setId(pmi.getPaymentMethodToken());
        return currentMoyenPaiement;
    }

    private String calculateExpiration(String expiryMonth, String expiryYear) {
        StringBuilder result = new StringBuilder();
        result.append(expiryMonth).append("/").append(expiryYear);
        return result.toString();
    }

    private String generateOrderId(String demarcheId) {
        StringBuilder result = new StringBuilder();
        result.append(demarcheId.toUpperCase()).append("-");
        result.append(RandomStringUtils.random(12, 0, 0, true, true, null, new SecureRandom()));
        return result.toString();

    }

    public InfoPaiementOutputDTO mwpaymtRegisterResponseToInfoPaiementOutputDTO(RegisterOutputDTO mwpaymtResponse) {
        InfoPaiementOutputDTO paiementOutputDTO = new InfoPaiementOutputDTO();
        paiementOutputDTO.setReference(mwpaymtResponse.getOrderId());
        //TODO pas de valeur fixe
        paiementOutputDTO.setStatus("SUCCESS");
        paiementOutputDTO.setAnswer(new AnswerDTO(mwpaymtResponse.getFormToken()));
        return paiementOutputDTO;
    }
}
