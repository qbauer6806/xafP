package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.dsp.dto.ResidLoyerPeriodiciteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidQualiteDTO;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidResidenceDLN1FDTO implements Serializable {
	
	private static final long serialVersionUID = -1720606882440326053L;

	private boolean locationLogement;
	
	private Integer nombreOccupant;

	private Integer loyer;

	private Integer nombrePiece;

	private Integer nombreStationnement;
	
	private Integer surfaceM2;
	
	private ResidQualiteDTO qualite;
	
	private ResidLoyerPeriodiciteDTO loyerPeriodicite;

}
