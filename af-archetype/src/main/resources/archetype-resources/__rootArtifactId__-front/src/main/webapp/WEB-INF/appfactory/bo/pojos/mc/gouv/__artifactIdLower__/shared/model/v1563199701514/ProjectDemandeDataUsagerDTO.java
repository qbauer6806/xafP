#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataUsagerDTO implements Serializable {

    private static final long serialVersionUID = 1563199701514L;
    private TitreEnum titre;
    private String nom;
    private String nomjeunefille;
    private String prenom;
    private String raisonsociale;
    private String mail;
    private String telephone;
    private ProjectDemandeDataUsagerAdresseDTO adresse;


    
    public TitreEnum getTitre() {
        return titre;
    }

    
    public void setTitre(TitreEnum value) {
        this.titre = value;
    }

    
    public String getNom() {
        return nom;
    }

    
    public void setNom(String value) {
        this.nom = value;
    }

    
    public String getNomjeunefille() {
        return nomjeunefille;
    }

    
    public void setNomjeunefille(String value) {
        this.nomjeunefille = value;
    }

    
    public String getPrenom() {
        return prenom;
    }

    
    public void setPrenom(String value) {
        this.prenom = value;
    }

    
    public String getRaisonsociale() {
        return raisonsociale;
    }

    
    public void setRaisonsociale(String value) {
        this.raisonsociale = value;
    }

    
    public String getMail() {
        return mail;
    }

    
    public void setMail(String value) {
        this.mail = value;
    }

    
    public String getTelephone() {
        return telephone;
    }

    
    public void setTelephone(String value) {
        this.telephone = value;
    }

    
    public ProjectDemandeDataUsagerAdresseDTO getAdresse() {
        return adresse;
    }

    
    public void setAdresse(ProjectDemandeDataUsagerAdresseDTO value) {
        this.adresse = value;
    }

}
