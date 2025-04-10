package mc.gouv.xaf.apiclient.paiement.monguichet.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReferencePostInputDTO {
    private String profileId;
    private String paymentMethodToken;
    private String paymentSupplier;
    private String tokenSupplier;
    private String paymentMethodName;
}
