package mc.gouv.xaf.shared.paiement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MwpaymtGenericCallbackDTO {
    private String reference;
    private String transactionId;
    private PaymentMethodInformationDTO paymentMethodInformation;
    private String orderId;
}
