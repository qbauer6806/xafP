package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * Cette classe représente un usager tel que retourné par l'API GICHUNI
 * 
 * @author qdeme
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GichuniUsagerDTO {

    private Integer id;
    private String login;
    private Short etat;
    private String email;
    private Short titre;
    private String prenom;
    private String nom;
    private String raisonSociale;
    private String adresse1;
    private String adresse2;
    private String complementAdresse;
    private String codePostal;
    private String ville;
    private String nomPays;
    private String paysId;
    private String paysCode;
    private UsagerTypeEnum type;

    protected JsonNode donneesExternes;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Short getEtat() {
        return etat;
    }

    public void setEtat(Short etat) {
        this.etat = etat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Short getTitre() {
        return titre;
    }

    public void setTitre(Short titre) {
        this.titre = titre;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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

    public String getComplementAdresse() {
        return complementAdresse;
    }

    public void setComplementAdresse(String complementAdresse) {
        this.complementAdresse = complementAdresse;
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

    public String getNomPays() {
        return nomPays;
    }

    public void setNomPays(String nomPays) {
        this.nomPays = nomPays;
    }

    public String getPaysId() {
        return paysId;
    }

    public void setPaysId(String paysId) {
        this.paysId = paysId;
    }

    public String getPaysCode() {
        return paysCode;
    }

    public void setPaysCode(String paysCode) {
        this.paysCode = paysCode;
    }

    public UsagerTypeEnum getType() {
        return type;
    }

    public void setType(UsagerTypeEnum type) {
        this.type = type;
    }

    public JsonNode getDonneesExternes() {
        return donneesExternes;
    }

    public void setDonneesExternes(JsonNode donneesExternes) {
        this.donneesExternes = donneesExternes;
    }

}
