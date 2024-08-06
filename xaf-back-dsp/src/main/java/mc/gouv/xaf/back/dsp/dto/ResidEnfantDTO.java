package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidSexeEnum;
import mc.gouv.xaf.back.dsp.enums.v2.ResidRelationEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidEnfantDTO implements Serializable {

	private static final long serialVersionUID = 8706466698730319490L;
	
	@Setter
    @Getter
    private ResidCiviliteEnum titreEnfant;

	@Setter
    @Getter
    private String nomEnfant;

	@Setter
    @Getter
    private String prenomEnfant;

	@Setter
    @Getter
    private String dateNaissanceEnfant;

	@Setter
    @Getter
    private String nationaliteEnfant;

	@Setter
    @Getter
    private ResidRelationEnum relationEnfant;

	@Setter
    @Getter
    private ResidSexeEnum sexeEnfant;
	
	@Setter
    @Getter
    private String lieuScolariteTravail;
	
	@Setter
    @Getter
    private boolean foyerEnfant;
	
	private Boolean autoriteParentaleEnfant;
	
	@Setter
    @Getter
    private ResidAdresseDTO adresseEnfant;

    public Boolean isAutoriteParentaleEnfant() {
		return autoriteParentaleEnfant;
	}

	public void setAutoriteParentaleEnfant(Boolean autoriteParentaleEnfant) {
		if(autoriteParentaleEnfant == null) {
			this.autoriteParentaleEnfant = true;
		}
		this.autoriteParentaleEnfant = autoriteParentaleEnfant;
	}

}
