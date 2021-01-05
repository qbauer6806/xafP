package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import mc.gouv.xaf.shared.itg.resid.enums.*;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidIdentiteDTO implements Serializable {

    private static final long serialVersionUID = -1741044093302861889L;

    private ResidCiviliteEnum civilite;

    private String nom;

    private String nomUsage;

    private String prenom;

    private String dateNaissance;

    private String heureNaissance;

    private String villeNaissance;

    private String paysNaissance;

    private ResidSexeEnum sexe;

    private ResidSituationFamilialeEnum situationFamiliale;

    private ResidNationaliteDTO nationalitePrincipale;

    @JsonInclude()
    private ResidNationaliteDTO nationaliteAutre;

    private String situationDate;

    private int nombreEnfants;

    private int nombreEnfantsFoyer;

    @JsonInclude()
    private String filiation;

    private String prefixeTelephonique;

    private String telephone;

    @JsonInclude()
    private String email;

    private ResidCanalCommunicationEnum canalCommunication;

    @JsonInclude()
    private ResidLanguePrefereeEnum languePreferee;

    public ResidCiviliteEnum getCivilite() {
        return civilite;
    }

    public void setCivilite(ResidCiviliteEnum civilite) {
        this.civilite = civilite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNomUsage() {
        return nomUsage;
    }

    public void setNomUsage(String nomUsage) {
        this.nomUsage = nomUsage;
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

    public String getHeureNaissance() {
        return heureNaissance;
    }

    public void setHeureNaissance(String heureNaissance) {
        this.heureNaissance = heureNaissance;
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

    public ResidSexeEnum getSexe() {
        return sexe;
    }

    public void setSexe(ResidSexeEnum sexe) {
        this.sexe = sexe;
    }

    public ResidSituationFamilialeEnum getSituationFamiliale() {
        return situationFamiliale;
    }

    public void setSituationFamiliale(ResidSituationFamilialeEnum situationFamiliale) {
        this.situationFamiliale = situationFamiliale;
    }

    public String getSituationDate() {
        return situationDate;
    }

    public void setSituationDate(String situationDate) {
        this.situationDate = situationDate;
    }

    public int getNombreEnfants() {
        return nombreEnfants;
    }

    public void setNombreEnfants(int nombreEnfants) {
        this.nombreEnfants = nombreEnfants;
    }

    public int getNombreEnfantsFoyer() {
        return nombreEnfantsFoyer;
    }

    public void setNombreEnfantsFoyer(int nombreEnfantsFoyer) {
        this.nombreEnfantsFoyer = nombreEnfantsFoyer;
    }

    public String getFiliation() {
        return filiation;
    }

    public void setFiliation(String filiation) {
        this.filiation = filiation;
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

    public ResidCanalCommunicationEnum getCanalCommunication() {
        return canalCommunication;
    }

    public void setCanalCommunication(ResidCanalCommunicationEnum canalCommunication) {
        this.canalCommunication = canalCommunication;
    }

    public ResidLanguePrefereeEnum getLanguePreferee() {
        return languePreferee;
    }

    public void setLanguePreferee(ResidLanguePrefereeEnum languePreferee) {
        this.languePreferee = languePreferee;
    }

    public ResidNationaliteDTO getNationalitePrincipale() {
        return nationalitePrincipale;
    }

    public void setNationalitePrincipale(ResidNationaliteDTO nationalitePrincipale) {
        this.nationalitePrincipale = nationalitePrincipale;
    }

    public ResidNationaliteDTO getNationaliteAutre() {
        return nationaliteAutre;
    }

    public void setNationaliteAutre(ResidNationaliteDTO nationaliteAutre) {
        this.nationaliteAutre = nationaliteAutre;
    }

    public String getPrefixeTelephonique() {
        return prefixeTelephonique;
    }

    public void setPrefixeTelephonique(String prefixeTelephonique) {
        this.prefixeTelephonique = prefixeTelephonique;
    }
}
