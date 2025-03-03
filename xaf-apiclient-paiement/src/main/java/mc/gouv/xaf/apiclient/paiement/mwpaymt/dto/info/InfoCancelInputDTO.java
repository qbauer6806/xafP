package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfoCancelInputDTO {

    @NotNull
    private String company;

    @NotNull
    private String transactionId;

    @NotNull
    private PaymentMethodInformationDTO paymentMethodInformation;

}
