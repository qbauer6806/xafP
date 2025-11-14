package mc.gouv.xaf.shared.paiement.infofacturation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdresseDTO {
    private String ligne1;
    private String ligne2;
    private String ligne3;
    private String codePostal;
    private String ville;
    private String pays;
}
