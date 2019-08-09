#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeVehiculeDTO implements Serializable {

    private static final long serialVersionUID = 1563199701514L;
    private OuinonEnum taxi;
    private EmissionVoitureEnum emissionvoiture;
    private Emission2RouesEnum emissiondeuxroues;
    private EmissionVeloEnum emissionvelo;
    private String nomproprietaire;
    private String prenomproprietaire;
    private OuinonEnum locationbatterie;


    
    public OuinonEnum getTaxi() {
        return taxi;
    }

    
    public void setTaxi(OuinonEnum value) {
        this.taxi = value;
    }

    
    public EmissionVoitureEnum getEmissionvoiture() {
        return emissionvoiture;
    }

    
    public void setEmissionvoiture(EmissionVoitureEnum value) {
        this.emissionvoiture = value;
    }

    
    public Emission2RouesEnum getEmissiondeuxroues() {
        return emissiondeuxroues;
    }

    
    public void setEmissiondeuxroues(Emission2RouesEnum value) {
        this.emissiondeuxroues = value;
    }

    
    public EmissionVeloEnum getEmissionvelo() {
        return emissionvelo;
    }

    
    public void setEmissionvelo(EmissionVeloEnum value) {
        this.emissionvelo = value;
    }

    
    public String getNomproprietaire() {
        return nomproprietaire;
    }

    
    public void setNomproprietaire(String value) {
        this.nomproprietaire = value;
    }

    
    public String getPrenomproprietaire() {
        return prenomproprietaire;
    }

    
    public void setPrenomproprietaire(String value) {
        this.prenomproprietaire = value;
    }

    
    public OuinonEnum getLocationbatterie() {
        return locationbatterie;
    }

    
    public void setLocationbatterie(OuinonEnum value) {
        this.locationbatterie = value;
    }

}
