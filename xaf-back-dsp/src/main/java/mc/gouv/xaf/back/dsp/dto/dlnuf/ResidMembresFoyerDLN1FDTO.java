package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidMembresFoyerDLN1FDTO implements Serializable {

	private static final long serialVersionUID = 5592048407903642368L;
	
	private boolean hasOtherPersonne;
	
	private List<ResidPersonneDLN1FDTO> personne;

	public boolean isHasOtherPersonne() {
		return hasOtherPersonne;
	}

	public void setHasOtherPersonne(boolean hasOtherPersonne) {
		this.hasOtherPersonne = hasOtherPersonne;
	}

	public List<ResidPersonneDLN1FDTO> getPersonne() {
		return personne;
	}

	public void setPersonne(List<ResidPersonneDLN1FDTO> personne) {
		this.personne = personne;
	}
}
