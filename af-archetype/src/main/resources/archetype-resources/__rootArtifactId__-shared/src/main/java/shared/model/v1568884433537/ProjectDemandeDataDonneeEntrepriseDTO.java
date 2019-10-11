#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1568884433537;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeEntrepriseDTO implements Serializable {

    private static final long serialVersionUID = 1568884433537L;
    private String numerocar;
    private String raisonsociale;
    private String nom;
    private ProjectDemandeDataDonneeEntrepriseAdresseDTO adresse;
    private String telephone;


    
    public String getNumerocar() {
        return numerocar;
    }

    
    public void setNumerocar(String value) {
        this.numerocar = value;
    }

    
    public String getRaisonsociale() {
        return raisonsociale;
    }

    
    public void setRaisonsociale(String value) {
        this.raisonsociale = value;
    }

    
    public String getNom() {
        return nom;
    }

    
    public void setNom(String value) {
        this.nom = value;
    }

    
    public ProjectDemandeDataDonneeEntrepriseAdresseDTO getAdresse() {
        return adresse;
    }

    
    public void setAdresse(ProjectDemandeDataDonneeEntrepriseAdresseDTO value) {
        this.adresse = value;
    }

    
    public String getTelephone() {
        return telephone;
    }

    
    public void setTelephone(String value) {
        this.telephone = value;
    }

}
