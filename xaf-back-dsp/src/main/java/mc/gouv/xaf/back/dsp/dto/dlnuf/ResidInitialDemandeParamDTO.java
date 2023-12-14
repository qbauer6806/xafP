package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;

public class ResidInitialDemandeParamDTO implements Serializable {

    private static final long serialVersionUID = 1151801717747924423L;

	private String nom;
	private String nomusage;
	private String prenom;
	private String dateNaissance;
	private String villeNaissance;
	private String paysNaissance;

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getNomusage() {
		return nomusage;
	}

	public void setNomusage(String nomusage) {
		this.nomusage = nomusage;
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

	public String getVilleNaissance() {
		return villeNaissance;
	}

	public void setVilleNaissance(String villeNaissance) {
		this.villeNaissance = villeNaissance;
	}

	public String getPaysNaissance() {
		return paysNaissance;
	}

	public void setPaysNaissance(String paysNaissance) {
		this.paysNaissance = paysNaissance;
	}

	@Override
	public String toString() {
		return "ResidInitialDemandeDTO{" +
				"nom='" + nom + '\'' +
				", nomusage='" + nomusage + '\'' +
				", prenom='" + prenom + '\'' +
				", dateNaissance='" + dateNaissance + '\'' +
				", villeNaissance='" + villeNaissance + '\'' +
				", paysNaissance='" + paysNaissance + '\'' +
				'}';
	}
}
