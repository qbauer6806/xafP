package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentInformationDTO {
    private String paymentMethodType;
    @JsonProperty(value = "3ds", required = true)
    private String threeDs;

}
