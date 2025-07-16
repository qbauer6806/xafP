package mc.gouv.xaf.shared.paiement;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.paiement.enums.PaymentMethodStatusEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentMethodInformationDTO {
    @NotNull
    private String paymentMethodType;
    @NotNull
    private String paymentMethodToken;
    private String pan;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String effectiveBrand;
    private PaymentMethodStatusEnum paymentMethodStatus;
    private String creationDate;
}
