package mc.gouv.xaf.shared.paiement.infofacturation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VousDTO {

    private String prenom;
    private String nom;
    private Short titre;
}
