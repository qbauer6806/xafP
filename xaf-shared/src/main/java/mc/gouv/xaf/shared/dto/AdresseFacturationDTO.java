package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdresseFacturationDTO {
    private String adresse;
    private String complAdresse1;
    private String complAdresse2;
    private String ville;
    private String codePostal;
    private String paysCode;
}
