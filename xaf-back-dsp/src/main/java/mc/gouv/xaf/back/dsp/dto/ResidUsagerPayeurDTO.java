package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidUsagerPayeurDTO {

    private String nom;
    private String prenom;
    private ResidCiviliteEnum civilite;
    private String courriel;
    private String raisonSociale;

}
