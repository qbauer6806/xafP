package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common.TransactionInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common.UserInformationDTO;

@Getter
@Setter
@ToString
public class RegisterInputDTO {

    private String redirectUri;
    private String action;
    private UserInformationDTO userInformation;
    private PaymentMethodInformationDTO paymentMethodInformation;
    private TransactionInformationDTO transactionInformation;
}
