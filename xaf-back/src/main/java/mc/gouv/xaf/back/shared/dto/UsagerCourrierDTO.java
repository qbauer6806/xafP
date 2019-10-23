package mc.gouv.xaf.back.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Modélise un usager courrier
 * 
 * @author qdeme
 *
 */
public class UsagerCourrierDTO {

    private Integer pkUsagersCourrier;

    private String demarcheId;

    private String login;

    private Integer titre;

    private String nom;

    private String prenom;

    private String raisonSociale;

    private String adresse1;

    private String adresse2;

    private String adresseComplement;

    private String codePostal;

    private String ville;

    private String pays;

    private String telephone;

    private String email;
    
    // Le contenu de l'access à créer dans le cas de la création d'un usager courrier
    private JsonNode accessContenu;
    
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateDerModif;
    
    @JsonIgnore
    private boolean updated = false;
    
    private int nbDemandes;

    public Integer getPkUsagersCourrier() {
        return pkUsagersCourrier;
    }

    public void setPkUsagersCourrier(Integer pkUsagersCourrier) {
        this.pkUsagersCourrier = pkUsagersCourrier;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Integer getTitre() {
        return titre;
    }

    public void setTitre(Integer titre) {
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

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
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

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateDerModif() {
        return dateDerModif;
    }

    public void setDateDerModif(Date dateDerModif) {
        this.dateDerModif = dateDerModif;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    
    public int getNbDemandes() {
        return nbDemandes;
    }

    
    public void setNbDemandes(int nbDemandes) {
        this.nbDemandes = nbDemandes;
    }

    
    public JsonNode getAccessContenu() {
        return accessContenu;
    }

    
    public void setAccessContenu(JsonNode accessContenu) {
        this.accessContenu = accessContenu;
    }
    
}
