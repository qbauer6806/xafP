package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.paiement.enums.PaymentMethodStatusEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class PaymentMethodInformationDTO {
    
    @NotNull
    private String paymentMethodType;

    @NotNull
    private String paymentMethodToken;

    private String pan;

    private String expiryMonth;

    private String expiryYear;

    private String effectiveBrand;

    private PaymentMethodStatusEnum paymentMethodStatus;

    private String creationDate;


}
