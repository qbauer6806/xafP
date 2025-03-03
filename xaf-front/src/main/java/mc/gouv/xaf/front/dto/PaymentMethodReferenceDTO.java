package mc.gouv.xaf.front.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentMethodReferenceDTO {

    //ex: CARD
    String paymentMethodType;

    // The payment method's alias
    String paymentMethodToken;

    // ex: My new card
    String paymentMethodName;
}
