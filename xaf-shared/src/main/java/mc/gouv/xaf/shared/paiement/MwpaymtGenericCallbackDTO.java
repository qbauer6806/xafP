package mc.gouv.xaf.shared.paiement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MwpaymtGenericCallbackDTO {
    private PaymentMethodInformationDTO paymentMethodInformation;
    private String orderId;
    private String sub;
}
