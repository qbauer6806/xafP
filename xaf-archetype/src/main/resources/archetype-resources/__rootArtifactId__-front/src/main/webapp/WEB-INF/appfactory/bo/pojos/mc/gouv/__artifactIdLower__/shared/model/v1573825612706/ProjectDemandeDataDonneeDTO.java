#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1573825612706;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeDTO implements Serializable {

    private static final long serialVersionUID = 1573825612706L;
    private ProjectDemandeDataDonneeEntrepriseDTO entreprise;
    private ProjectDemandeDataDonneeSalariesDTO salaries;
    private ProjectDemandeDataDonneeDemandeurDTO demandeur;
    private ProjectDemandeDataDonneeEntrepriseorigineDTO entrepriseorigine;


    
    public ProjectDemandeDataDonneeEntrepriseDTO getEntreprise() {
        return entreprise;
    }

    
    public void setEntreprise(ProjectDemandeDataDonneeEntrepriseDTO value) {
        this.entreprise = value;
    }

    
    public ProjectDemandeDataDonneeSalariesDTO getSalaries() {
        return salaries;
    }

    
    public void setSalaries(ProjectDemandeDataDonneeSalariesDTO value) {
        this.salaries = value;
    }

    
    public ProjectDemandeDataDonneeDemandeurDTO getDemandeur() {
        return demandeur;
    }

    
    public void setDemandeur(ProjectDemandeDataDonneeDemandeurDTO value) {
        this.demandeur = value;
    }

    
    public ProjectDemandeDataDonneeEntrepriseorigineDTO getEntrepriseorigine() {
        return entrepriseorigine;
    }

    
    public void setEntrepriseorigine(ProjectDemandeDataDonneeEntrepriseorigineDTO value) {
        this.entrepriseorigine = value;
    }

}
