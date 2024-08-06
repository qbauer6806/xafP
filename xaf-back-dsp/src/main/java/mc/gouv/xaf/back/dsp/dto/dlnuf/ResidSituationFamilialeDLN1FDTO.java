package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidSituationFamilialeEnum;
import mc.gouv.xaf.back.dsp.enums.v2.ResidRelationEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidSituationFamilialeDLN1FDTO implements Serializable {

	private static final long serialVersionUID = -2369443223981508962L;

	private ResidSituationFamilialeEnum situationFamiliale;

	private ResidCiviliteEnum titre;

	private String nom;

	private String prenom;

	private String dateNaissance;

	private String nationalite;

	private ResidRelationEnum relation;

	private boolean foyer;
	
	private String lieuNomEntreprise;

}
