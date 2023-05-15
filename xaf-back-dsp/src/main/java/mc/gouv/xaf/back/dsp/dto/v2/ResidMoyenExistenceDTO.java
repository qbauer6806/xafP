package mc.gouv.xaf.back.dsp.dto.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidMoyenExistenceDTO implements Serializable {

	private static final long serialVersionUID = -4734685866364308622L;

	private String situationPrincipale;

	private String employeurRaisonSociale;

	private String employeurVille;

	private String employeurPays;

	public String getSituationPrincipale() {
		return situationPrincipale;
	}

	public void setSituationPrincipale(String situationPrincipale) {
		this.situationPrincipale = situationPrincipale;
	}

	public String getEmployeurRaisonSociale() {
		return employeurRaisonSociale;
	}

	public void setEmployeurRaisonSociale(String employeurRaisonSociale) {
		this.employeurRaisonSociale = employeurRaisonSociale;
	}

	public String getEmployeurVille() {
		return employeurVille;
	}

	public void setEmployeurVille(String employeurVille) {
		this.employeurVille = employeurVille;
	}

	public String getEmployeurPays() {
		return employeurPays;
	}

	public void setEmployeurPays(String employeurPays) {
		this.employeurPays = employeurPays;
	}

	@Override
	public String toString() {
		return "ResidMoyenExistenceDTO{" + "situationPrincipale='" + situationPrincipale + '\''
				+ ", employeurRaisonSociale='" + employeurRaisonSociale + '\'' + ", employeurVille='" + employeurVille
				+ '\'' + ", employeurPays='" + employeurPays + '}';
	}

}
