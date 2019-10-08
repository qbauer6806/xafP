#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1568884433537;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeDTO implements Serializable {

    private static final long serialVersionUID = 1568884433537L;
    private ProjectDemandeDataDonneeEntrepriseDTO entreprise;
    private ProjectDemandeDataDonneeDemandeurDTO demandeur;
    private ProjectDemandeDataDonneeDerogationDTO derogation;


    
    public ProjectDemandeDataDonneeEntrepriseDTO getEntreprise() {
        return entreprise;
    }

    
    public void setEntreprise(ProjectDemandeDataDonneeEntrepriseDTO value) {
        this.entreprise = value;
    }

    
    public ProjectDemandeDataDonneeDemandeurDTO getDemandeur() {
        return demandeur;
    }

    
    public void setDemandeur(ProjectDemandeDataDonneeDemandeurDTO value) {
        this.demandeur = value;
    }

    
    public ProjectDemandeDataDonneeDerogationDTO getDerogation() {
        return derogation;
    }

    
    public void setDerogation(ProjectDemandeDataDonneeDerogationDTO value) {
        this.derogation = value;
    }

}
