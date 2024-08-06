package mc.gouv.xaf.back.dsp.dto.dlnuf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.dsp.dto.ResidAdresseDTO;
import mc.gouv.xaf.back.dsp.dto.ResidMoyensExistenceDTO;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidUsagerNpdhlDTO implements Serializable {

    private static final long serialVersionUID = 1151801717747924423L;
    
    private ResidIdentiteDLN1FDTO identite;
    
	private ResidContactDNL1FDTO contacts;
    
    private ResidResidenceDLN1FDTO residence;
    
    private ResidMoyensExistenceDTO moyenExistence;
    
    @JsonInclude()
    private ResidAdresseDTO adresse;
    
    private ResidNationalite1Et2DTO nationalite;
    
    private ResidResidentDLN1FDTO resident;
    
    private ResidSituationFamilialeDLN1FDTO situationFamiliale;
    
    @JsonInclude()
    private ResidEnfantsDLN1FDTO enfants;
    
    @JsonInclude()
    private ResidMembresFoyerDLN1FDTO membresFoyer;


}
