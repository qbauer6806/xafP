package mc.gouv.xaf.shared.paiement.infopaiement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class InfoPaiementInputDTO {
    private String demandesId;
    private boolean iframe;
    private String langue;
    private String providerName;
    private String raisonSociale;
}
