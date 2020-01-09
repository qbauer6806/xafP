#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1573825612706;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeEntrepriseorigineDTO implements Serializable {

    private static final long serialVersionUID = 1573825612706L;
    private String raisonsociale;
    private ProjectDemandeDataDonneeEntrepriseorigineAdresseDTO adresse;


    
    public String getRaisonsociale() {
        return raisonsociale;
    }

    
    public void setRaisonsociale(String value) {
        this.raisonsociale = value;
    }

    
    public ProjectDemandeDataDonneeEntrepriseorigineAdresseDTO getAdresse() {
        return adresse;
    }

    
    public void setAdresse(ProjectDemandeDataDonneeEntrepriseorigineAdresseDTO value) {
        this.adresse = value;
    }

}
