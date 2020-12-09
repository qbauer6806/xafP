package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidEtatsDemandesUpdatedAfterDTO implements Serializable {

	private static final long serialVersionUID = -4873508865715945892L;

	private List<ResidStatutDemandeDTO> etatsDemandes;

	private String lastUpdateHorodatage;

	private Boolean moreUpdates;

	public List<ResidStatutDemandeDTO> getEtatsDemandes() {
		return etatsDemandes;
	}
	
	public void setEtatsDemandes(List<ResidStatutDemandeDTO> etatsDemandes) {
		this.etatsDemandes = etatsDemandes;
	}
	
	public String getLastUpdateHorodatage() {
		return lastUpdateHorodatage;
	}
	
	public void setLastUpdateHorodatage(String lastUpdateHorodatage) {
		this.lastUpdateHorodatage = lastUpdateHorodatage;
	}
	
	public Boolean getMoreUpdates() {
		return moreUpdates;
	}
	
	public void setMoreUpdates(Boolean moreUpdates) {
		this.moreUpdates = moreUpdates;
	}
}
