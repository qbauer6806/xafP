package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.dto.ResidEnfantDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidEnfantsDLN1FDTO implements Serializable {

	private static final long serialVersionUID = 4037812219398491343L;

	private boolean hasEnfantMineur;
	
	private Integer nombreEnfantFoyer;
	
	private Integer nombreEnfantTotal;
	
	private List<ResidEnfantDTO> enfants;

	public boolean isHasEnfantMineur() {
		return hasEnfantMineur;
	}

	public void setHasEnfantMineur(boolean hasEnfantMineur) {
		this.hasEnfantMineur = hasEnfantMineur;
	}

	public Integer getNombreEnfantFoyer() {
		return nombreEnfantFoyer;
	}

	public void setNombreEnfantFoyer(Integer nombreEnfantFoyer) {
		this.nombreEnfantFoyer = nombreEnfantFoyer;
	}

	public Integer getNombreEnfantTotal() {
		return nombreEnfantTotal;
	}

	public void setNombreEnfantTotal(Integer nombreEnfantTotal) {
		this.nombreEnfantTotal = nombreEnfantTotal;
	}

	public List<ResidEnfantDTO> getEnfants() {
		return enfants;
	}

	public void setEnfants(List<ResidEnfantDTO> enfants) {
		this.enfants = enfants;
	}
	
	
}
