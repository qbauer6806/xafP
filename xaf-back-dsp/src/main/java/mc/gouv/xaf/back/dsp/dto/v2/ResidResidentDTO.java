package mc.gouv.xaf.back.dsp.dto.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidResidentDTO implements Serializable {

	private static final long serialVersionUID = 3142537019985412980L;

	private String numeroCarte;

	private String dateDebutValidite;

	private String dateFinValidite;

	private String type;

	private String dateEtablissementMonaco;

	public String getNumeroCarte() {
		return numeroCarte;
	}

	public void setNumeroCarte(String numeroCarte) {
		this.numeroCarte = numeroCarte;
	}

	public String getDateDebutValidite() {
		return dateDebutValidite;
	}

	public void setDateDebutValidite(String dateDebutValidite) {
		this.dateDebutValidite = dateDebutValidite;
	}

	public String getDateFinValidite() {
		return dateFinValidite;
	}

	public void setDateFinValidite(String dateFinValidite) {
		this.dateFinValidite = dateFinValidite;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDateEtablissementMonaco() {
		return dateEtablissementMonaco;
	}

	public void setDateEtablissementMonaco(String dateEtablissementMonaco) {
		this.dateEtablissementMonaco = dateEtablissementMonaco;
	}

	@Override
	public String toString() {
		return "ResidResidentDTO{" + "numeroCarte='" + numeroCarte + '\'' + ", dateDebutValidite='" + dateDebutValidite
				+ '\'' + ", dateFinValidite='" + dateFinValidite + '\'' + ", type='" + type + '\''
				+ ", dateEtablissementMonaco='" + dateEtablissementMonaco + '}';
	}

}
