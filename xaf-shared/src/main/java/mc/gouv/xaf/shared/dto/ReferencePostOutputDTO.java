package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReferencePostOutputDTO {
    private String profileId;
    private String paymentMethodToken;
    private String paymentMethodType;
    private String paymentSupplier;
    private String tokenSupplier;
    private String paymentMethodName;
}
