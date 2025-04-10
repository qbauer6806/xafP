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
    private String orderId;
    private String cardName;
}
