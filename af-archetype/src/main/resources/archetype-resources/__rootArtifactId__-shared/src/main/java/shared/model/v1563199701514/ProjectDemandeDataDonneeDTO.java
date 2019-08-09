#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeDTO implements Serializable {

    private static final long serialVersionUID = 1563199701514L;
    private TypeUsagerEnum vehiculetypeusager;
    private VehiculeTypetousEnum vehiculetypetous;
    private ProjectDemandeDataDonneeVehiculeDTO vehicule;
    private ProjectDemandeDataDonneeDeclarationDTO declaration;
    private ProjectDemandeDataDonneeSimulationDTO simulation;
    private String prixbase;
    private String locationbatterie;
    private String simulationtva;
    private String simulationprixtotalvehicule;
    private String simulationprixapplication30;
    private ProjectDemandeDataDonneeBancaireDTO bancaire;


    
    public TypeUsagerEnum getVehiculetypeusager() {
        return vehiculetypeusager;
    }

    
    public void setVehiculetypeusager(TypeUsagerEnum value) {
        this.vehiculetypeusager = value;
    }

    
    public VehiculeTypetousEnum getVehiculetypetous() {
        return vehiculetypetous;
    }

    
    public void setVehiculetypetous(VehiculeTypetousEnum value) {
        this.vehiculetypetous = value;
    }

    
    public ProjectDemandeDataDonneeVehiculeDTO getVehicule() {
        return vehicule;
    }

    
    public void setVehicule(ProjectDemandeDataDonneeVehiculeDTO value) {
        this.vehicule = value;
    }

    
    public ProjectDemandeDataDonneeDeclarationDTO getDeclaration() {
        return declaration;
    }

    
    public void setDeclaration(ProjectDemandeDataDonneeDeclarationDTO value) {
        this.declaration = value;
    }

    
    public ProjectDemandeDataDonneeSimulationDTO getSimulation() {
        return simulation;
    }

    
    public void setSimulation(ProjectDemandeDataDonneeSimulationDTO value) {
        this.simulation = value;
    }

    
    public String getPrixbase() {
        return prixbase;
    }

    
    public void setPrixbase(String value) {
        this.prixbase = value;
    }

    
    public String getLocationbatterie() {
        return locationbatterie;
    }

    
    public void setLocationbatterie(String value) {
        this.locationbatterie = value;
    }

    
    public String getSimulationtva() {
        return simulationtva;
    }

    
    public void setSimulationtva(String value) {
        this.simulationtva = value;
    }

    
    public String getSimulationprixtotalvehicule() {
        return simulationprixtotalvehicule;
    }

    
    public void setSimulationprixtotalvehicule(String value) {
        this.simulationprixtotalvehicule = value;
    }

    
    public String getSimulationprixapplication30() {
        return simulationprixapplication30;
    }

    
    public void setSimulationprixapplication30(String value) {
        this.simulationprixapplication30 = value;
    }

    
    public ProjectDemandeDataDonneeBancaireDTO getBancaire() {
        return bancaire;
    }

    
    public void setBancaire(ProjectDemandeDataDonneeBancaireDTO value) {
        this.bancaire = value;
    }

}
