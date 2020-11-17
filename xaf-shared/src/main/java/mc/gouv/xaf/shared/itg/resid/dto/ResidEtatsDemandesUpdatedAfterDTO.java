package mc.gouv.xaf.shared.itg.resid.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidEtatsDemandesUpdatedAfterDTO implements Serializable {

	private static final long serialVersionUID = -2562015127165905351L;
	private List<ResidStatutDemandeDTO> etatsDemandes;
	private String lastUpdateHorodatage;
	private Boolean moreUpdates;
	private int httpStatus;
    private String message;
    private List<Error> errors;

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }
	
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
