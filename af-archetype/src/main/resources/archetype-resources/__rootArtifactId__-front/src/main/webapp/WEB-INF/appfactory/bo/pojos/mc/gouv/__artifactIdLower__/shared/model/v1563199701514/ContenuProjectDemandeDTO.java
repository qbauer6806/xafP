#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ContenuProjectDemandeDTO implements Serializable {

    private static final long serialVersionUID = 1563199701514L;
    private ProjectDemandeDataUsagerDTO usager;
    private ProjectDemandeDataDonneeDTO donnee;
    private ProjectDemandeDataVehiculeDTO vehicule;
    private ProjectDemandeDataDeclarationDTO declaration;
    private ProjectDemandeDataSimulationDTO simulation;


    
    public ProjectDemandeDataUsagerDTO getUsager() {
        return usager;
    }

    
    public void setUsager(ProjectDemandeDataUsagerDTO value) {
        this.usager = value;
    }

    
    public ProjectDemandeDataDonneeDTO getDonnee() {
        return donnee;
    }

    
    public void setDonnee(ProjectDemandeDataDonneeDTO value) {
        this.donnee = value;
    }

    
    public ProjectDemandeDataVehiculeDTO getVehicule() {
        return vehicule;
    }

    
    public void setVehicule(ProjectDemandeDataVehiculeDTO value) {
        this.vehicule = value;
    }

    
    public ProjectDemandeDataDeclarationDTO getDeclaration() {
        return declaration;
    }

    
    public void setDeclaration(ProjectDemandeDataDeclarationDTO value) {
        this.declaration = value;
    }

    
    public ProjectDemandeDataSimulationDTO getSimulation() {
        return simulation;
    }

    
    public void setSimulation(ProjectDemandeDataSimulationDTO value) {
        this.simulation = value;
    }

}
