#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1568884433537;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeDemandeurDTO implements Serializable {

    private static final long serialVersionUID = 1568884433537L;
    private TitreEnum titre;
    private String prenom;
    private String nom;
    private String mail;


    
    public TitreEnum getTitre() {
        return titre;
    }

    
    public void setTitre(TitreEnum value) {
        this.titre = value;
    }

    
    public String getPrenom() {
        return prenom;
    }

    
    public void setPrenom(String value) {
        this.prenom = value;
    }

    
    public String getNom() {
        return nom;
    }

    
    public void setNom(String value) {
        this.nom = value;
    }

    
    public String getMail() {
        return mail;
    }

    
    public void setMail(String value) {
        this.mail = value;
    }

}
