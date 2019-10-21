package mc.gouv.af.backweb.formbean;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Formulaire pour les usagers courrier
 * 
 * @author qdeme
 * 
 */
public class UsagerCourrierFormBean {

	private String titre;

	private String nom;

	private String prenom;

	private String raisonSociale;

	@NotBlank
	@NotNull(message = "L'adresse doit être précisé")
	@Size(min = 1, max = 128, message = "L'adresse doit avoir une taille comprise entre 1 et 128")
	private String adresse1;

	private String adresse2;

	private String adresseComplement;

	@NotBlank
	@NotNull(message = "Le code postal doit être précisé")
	@Size(min = 1, max = 10, message = "Le code postal doit avoir une taille comprise entre 1 et 10")
	private String codePostal;

	@NotBlank
	@NotNull(message = "La ville doit être précisée")
	@Size(min = 1, max = 50, message = "La ville doit avoir une taille comprise entre 1 et 50")
	private String ville;

	@Size(min = 0, max = 64, message = "Le numéro de téléphone doit avoir une taille comprise entre 0 et 64")
	private String telephone;

	@Size(min = 0, max = 256, message = "L'adresse email doit avoir une taille comprise entre 0 et 256")
	private String email;

	@NotBlank(message = "Le pays doit être précisé")
	private String paysChoisi;

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

	public String getRaisonSociale() {
		return raisonSociale;
	}

	public void setRaisonSociale(String raisonSociale) {
		this.raisonSociale = raisonSociale;
	}

	public String getAdresse1() {
		return adresse1;
	}

	public void setAdresse1(String adresse1) {
		this.adresse1 = adresse1;
	}

	public String getAdresse2() {
		return adresse2;
	}

	public void setAdresse2(String adresse2) {
		this.adresse2 = adresse2;
	}

	public String getAdresseComplement() {
		return adresseComplement;
	}

	public void setAdresseComplement(String adresseComplement) {
		this.adresseComplement = adresseComplement;
	}

	public String getCodePostal() {
		return codePostal;
	}

	public void setCodePostal(String codePostal) {
		this.codePostal = codePostal;
	}

	public String getVille() {
		return ville;
	}

	public void setVille(String ville) {
		this.ville = ville;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getPaysChoisi() {
		return paysChoisi;
	}

	public void setPaysChoisi(String paysChoisi) {
		this.paysChoisi = paysChoisi;
	}

}
