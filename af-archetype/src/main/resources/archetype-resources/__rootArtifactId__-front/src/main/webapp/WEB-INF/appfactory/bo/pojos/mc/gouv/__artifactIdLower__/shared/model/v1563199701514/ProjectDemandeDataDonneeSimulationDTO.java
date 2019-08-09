#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeSimulationDTO implements Serializable {

    private static final long serialVersionUID = 1563199701514L;
    private String reglecalculmoins20quatresroues;
    private String reglecalculmoins20deuxroues;
    private String reglecalculmoins20velo;
    private String montantaide4roues61;
    private String montantaide4roues51;
    private String montantaide4roues21;
    private String montantaide2roues21;
    private String remises;
    private String primetaxi;


    
    public String getReglecalculmoins20quatresroues() {
        return reglecalculmoins20quatresroues;
    }

    
    public void setReglecalculmoins20quatresroues(String value) {
        this.reglecalculmoins20quatresroues = value;
    }

    
    public String getReglecalculmoins20deuxroues() {
        return reglecalculmoins20deuxroues;
    }

    
    public void setReglecalculmoins20deuxroues(String value) {
        this.reglecalculmoins20deuxroues = value;
    }

    
    public String getReglecalculmoins20velo() {
        return reglecalculmoins20velo;
    }

    
    public void setReglecalculmoins20velo(String value) {
        this.reglecalculmoins20velo = value;
    }

    
    public String getMontantaide4roues61() {
        return montantaide4roues61;
    }

    
    public void setMontantaide4roues61(String value) {
        this.montantaide4roues61 = value;
    }

    
    public String getMontantaide4roues51() {
        return montantaide4roues51;
    }

    
    public void setMontantaide4roues51(String value) {
        this.montantaide4roues51 = value;
    }

    
    public String getMontantaide4roues21() {
        return montantaide4roues21;
    }

    
    public void setMontantaide4roues21(String value) {
        this.montantaide4roues21 = value;
    }

    
    public String getMontantaide2roues21() {
        return montantaide2roues21;
    }

    
    public void setMontantaide2roues21(String value) {
        this.montantaide2roues21 = value;
    }

    
    public String getRemises() {
        return remises;
    }

    
    public void setRemises(String value) {
        this.remises = value;
    }

    
    public String getPrimetaxi() {
        return primetaxi;
    }

    
    public void setPrimetaxi(String value) {
        this.primetaxi = value;
    }

}
