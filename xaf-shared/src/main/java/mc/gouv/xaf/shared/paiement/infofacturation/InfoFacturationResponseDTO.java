package mc.gouv.xaf.shared.paiement.infofacturation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoFacturationResponseDTO {

    private VousDTO vous;
    private String email;
    private AdresseDTO adresse;
    private String raisonSociale;
    private boolean saveRaisonSociale;
    private String profilType;
}
