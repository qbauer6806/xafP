package mc.gouv.xaf.shared.paiement.moyenpaiement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MoyenPaiementInputDTO {
    @JsonProperty(required = true)
    private String reference;
    @JsonProperty(required = true)
    private String cardName;
    @JsonProperty(required = true)
    private boolean isNew;
    private String paymentMethodToken;
}
