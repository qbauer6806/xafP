#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataSimulationDTO implements Serializable {

    private static final long serialVersionUID = 1563199701514L;
    private String montant;


    
    public String getMontant() {
        return montant;
    }

    
    public void setMontant(String value) {
        this.montant = value;
    }

}
