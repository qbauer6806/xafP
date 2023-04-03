package mc.gouv.xaf.back.dsp.dto.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.ResidRelationEnum;
import mc.gouv.xaf.back.dsp.enums.ResidSexeEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidEnfantDTO implements Serializable {

	private static final long serialVersionUID = 8706466698730319490L;
	
	private ResidCiviliteEnum titreEnfant;

	private String nomEnfant;

	private String prenomEnfant;

	private String dateNaissanceEnfant;

	private String nationaliteEnfant;

	private ResidRelationEnum relationEnfant;

	private ResidSexeEnum sexeEnfant;
	
	private String lieuScolariteTravail;
	
	private boolean foyerEnfant;
	
	private boolean autoriteParentaleEnfant;
	
	private ResidAdresseDTO adresseEnfant;

	public ResidCiviliteEnum getTitreEnfant() {
		return titreEnfant;
	}

	public void setTitreEnfant(ResidCiviliteEnum titreEnfant) {
		this.titreEnfant = titreEnfant;
	}

	public String getNomEnfant() {
		return nomEnfant;
	}

	public void setNomEnfant(String nomEnfant) {
		this.nomEnfant = nomEnfant;
	}

	public String getPrenomEnfant() {
		return prenomEnfant;
	}

	public void setPrenomEnfant(String prenomEnfant) {
		this.prenomEnfant = prenomEnfant;
	}

	public String getDateNaissanceEnfant() {
		return dateNaissanceEnfant;
	}

	public void setDateNaissanceEnfant(String dateNaissanceEnfant) {
		this.dateNaissanceEnfant = dateNaissanceEnfant;
	}

	public String getNationaliteEnfant() {
		return nationaliteEnfant;
	}

	public void setNationaliteEnfant(String nationaliteEnfant) {
		this.nationaliteEnfant = nationaliteEnfant;
	}

	public ResidRelationEnum getRelationEnfant() {
		return relationEnfant;
	}

	public void setRelationEnfant(ResidRelationEnum relationEnfant) {
		this.relationEnfant = relationEnfant;
	}

	public ResidSexeEnum getSexeEnfant() {
		return sexeEnfant;
	}

	public void setSexeEnfant(ResidSexeEnum sexeEnfant) {
		this.sexeEnfant = sexeEnfant;
	}

	public String getLieuScolariteTravail() {
		return lieuScolariteTravail;
	}

	public void setLieuScolariteTravail(String lieuScolariteTravail) {
		this.lieuScolariteTravail = lieuScolariteTravail;
	}

	public boolean isFoyerEnfant() {
		return foyerEnfant;
	}

	public void setFoyerEnfant(boolean foyerEnfant) {
		this.foyerEnfant = foyerEnfant;
	}

	public boolean isAutoriteParentaleEnfant() {
		return autoriteParentaleEnfant;
	}

	public void setAutoriteParentaleEnfant(boolean autoriteParentaleEnfant) {
		this.autoriteParentaleEnfant = autoriteParentaleEnfant;
	}

	public ResidAdresseDTO getAdresseEnfant() {
		return adresseEnfant;
	}

	public void setAdresseEnfant(ResidAdresseDTO adresseEnfant) {
		this.adresseEnfant = adresseEnfant;
	}
	
	@Override
	public String toString() {
		return "ResidEnfantDTO{" + "titreEnfant='" + titreEnfant + '\'' + ", nomEnfant='" + nomEnfant + '\''
				+ ", prenomEnfant='" + prenomEnfant + '\'' + ", dateNaissanceEnfant='" + dateNaissanceEnfant + '\''
				+ ", nationaliteEnfant='" + nationaliteEnfant + '\'' + ", relationEnfant='"
				+ relationEnfant + '\'' + ", foyerEnfant='" + foyerEnfant + '\''
				+ ", sexeEnfant='" + sexeEnfant + '\'' + ", autoriteParentaleEnfant=" + autoriteParentaleEnfant + '\''
				+ ", lieuScolariteTravail='" + lieuScolariteTravail + '\''
				+ ", adresseEnfant=" + adresseEnfant.toString() + '}';
	}

}
