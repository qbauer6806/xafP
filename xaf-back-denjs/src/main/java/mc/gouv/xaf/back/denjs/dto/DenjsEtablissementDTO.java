package mc.gouv.xaf.back.denjs.dto;

/**
 * DTO représentant un établissement scolaire
 * 
 * @author qdeme
 *
 */
public class DenjsEtablissementDTO {

    private String code;

    private String nom;

    private String nomPhrase;

    private String adresse;

    private String telephone;

    private String email;

    public String getCode() {
	return code;
    }

    public void setCode(String code) {
	this.code = code;
    }

    public String getNom() {
	return nom;
    }

    public void setNom(String nom) {
	this.nom = nom;
    }

    public String getNomPhrase() {
	return nomPhrase;
    }

    public void setNomPhrase(String nomPhrase) {
	this.nomPhrase = nomPhrase;
    }

    public String getAdresse() {
	return adresse;
    }

    public void setAdresse(String adresse) {
	this.adresse = adresse;
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

}
