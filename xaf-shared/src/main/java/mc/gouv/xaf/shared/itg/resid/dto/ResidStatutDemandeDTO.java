package mc.gouv.xaf.shared.itg.resid.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import mc.gouv.xaf.shared.itg.resid.enums.ResidEtatEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidStatutDemandeDTO implements Serializable {

    private static final long serialVersionUID = 1737364601317166129L;

    private String idTs;

    private String etat;
    
    private int httpStatus;
	private String message;
    private List<Error> errors;
    
    // TODO Resid à supprimer car le timestamp sera connu de resid, mais pour le moment j'ai besoin d'un élément de comparaison
    private String timestamp;

    public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getIdTs() {
        return idTs;
    }

    public void setIdTs(String idTs) {
        this.idTs = idTs;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }
    
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
}
