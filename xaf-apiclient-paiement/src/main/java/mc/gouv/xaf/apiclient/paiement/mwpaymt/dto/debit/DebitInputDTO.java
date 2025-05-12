package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common.TransactionInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common.UserInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.PaymentMethodInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.enums.ActionEnum;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebitInputDTO {

    @NotNull
    private UserInformationDTO userInformation;

    @NotNull
    private String paymentMethodToken;

    private ActionEnum action;

    @NotNull
    private TransactionInformationDTO transactionInformation;
}
