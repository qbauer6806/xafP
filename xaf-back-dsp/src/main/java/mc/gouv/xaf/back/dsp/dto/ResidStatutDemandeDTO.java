package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.common.ResidEtatEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidStatutDemandeDTO implements Serializable {

    private static final long serialVersionUID = 1737364601317166129L;

    private String idTS;

    private ResidEtatEnum etat;
    
    // TODO Resid à supprimer car le timestamp sera connu de resid, mais pour le moment j'ai besoin d'un élément de comparaison
    private String timestamp;

    public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getIdTS() {
        return idTS;
    }

    public void setIdTS(String idTS) {
        this.idTS = idTS;
    }

    public ResidEtatEnum getEtat() {
        return etat;
    }

    public void setEtat(ResidEtatEnum etat) {
        this.etat = etat;
    }

	@Override
	public String toString() {
		return "ResidStatutDemandeDTO{" +
				"idTS='" + idTS + '\'' +
				", etat=" + etat +
				'}';
	}
}
