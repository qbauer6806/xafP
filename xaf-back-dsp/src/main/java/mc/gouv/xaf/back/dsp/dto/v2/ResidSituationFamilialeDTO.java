package mc.gouv.xaf.back.dsp.dto.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidSituationFamilialeDTO implements Serializable {

	private static final long serialVersionUID = -2369443223981508962L;

	private String situationFamiliale;

	private String titre;

	private String nom;

	private String prenom;

	private String dateNaissance;

	private String nationalite;

	private String relation;

	private boolean foyer;

	public String getSituationFamiliale() {
		return situationFamiliale;
	}

	public void setSituationFamiliale(String situationFamiliale) {
		this.situationFamiliale = situationFamiliale;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
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

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
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
