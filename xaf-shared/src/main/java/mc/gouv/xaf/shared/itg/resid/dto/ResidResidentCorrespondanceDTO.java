package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidResidentCorrespondanceDTO implements Serializable {

    private static final long serialVersionUID = 1151801717747924423L;

    private String numeroUsager;

    private String nom;

    private String nomUsage;

    private String prenom;

    private String dateNaissance;

    private String villeNaissance;

    private String paysNaissance;

    private ResidAdresseDTO adresse;

    public String getNumeroUsager() {
        return numeroUsager;
    }

    public void setNumeroUsager(String numeroUsager) {
        this.numeroUsager = numeroUsager;
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

    public ResidAdresseDTO getAdresse() {
        return adresse;
    }

    public void setAdresse(ResidAdresseDTO adresse) {
        this.adresse = adresse;
    }
}
