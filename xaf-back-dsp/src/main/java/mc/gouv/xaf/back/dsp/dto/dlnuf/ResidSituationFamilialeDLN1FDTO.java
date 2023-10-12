package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidSituationFamilialeEnum;
import mc.gouv.xaf.back.dsp.enums.v2.ResidRelationEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidSituationFamilialeDLN1FDTO implements Serializable {

	private static final long serialVersionUID = -2369443223981508962L;

	private ResidSituationFamilialeEnum situationFamiliale;

	private ResidCiviliteEnum titre;

	private String nom;

	private String prenom;

	private String dateNaissance;

	private String nationalite;

	private ResidRelationEnum relation;

	private boolean foyer;

	public ResidSituationFamilialeEnum getSituationFamiliale() {
		return situationFamiliale;
	}

	public void setSituationFamiliale(ResidSituationFamilialeEnum situationFamiliale) {
		this.situationFamiliale = situationFamiliale;
	}

	public ResidCiviliteEnum getTitre() {
		return titre;
	}

	public void setTitre(ResidCiviliteEnum titre) {
		this.titre = titre;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getDateNaissance() {
		return dateNaissance;
	}

	public void setDateNaissance(String dateNaissance) {
		this.dateNaissance = dateNaissance;
	}

	public String getNationalite() {
		return nationalite;
	}

	public void setNationalite(String nationalite) {
		this.nationalite = nationalite;
	}

	public ResidRelationEnum getRelation() {
		return relation;
	}

	public void setRelation(ResidRelationEnum relation) {
		this.relation = relation;
	}

	public boolean isFoyer() {
		return foyer;
	}

	public void setFoyer(boolean foyer) {
		this.foyer = foyer;
	}

	@Override
	public String toString() {
		return "ResidSituationFamilialeDTO{" + "situationFamiliale='" + situationFamiliale + '\'' + ", titre='" + titre
				+ '\'' + ", nom='" + nom + '\'' + ", prenom='" + prenom + '\'' + ", dateNaissance='" + dateNaissance
				+ ", nationalite='" + nationalite + ", relation='" + relation + ", foyer='" + foyer + '}';
	}
}
