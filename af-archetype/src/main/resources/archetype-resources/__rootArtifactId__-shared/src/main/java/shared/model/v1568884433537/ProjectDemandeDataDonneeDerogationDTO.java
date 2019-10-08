#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1568884433537;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeDerogationDTO implements Serializable {

    private static final long serialVersionUID = 1568884433537L;
    private ProjectDemandeFieldDonneeDerogationJoursferiesDTO joursferies;
    private ProjectDemandeDataDonneeDerogationEmployeDTO employe;
    private String motivationdemande;
    private OuinonEnum presencedeleguespersonnel;
    private String nombresalarie;


    
    public ProjectDemandeFieldDonneeDerogationJoursferiesDTO getJoursferies() {
        return joursferies;
    }

    
    public void setJoursferies(ProjectDemandeFieldDonneeDerogationJoursferiesDTO value) {
        this.joursferies = value;
    }

    
    public ProjectDemandeDataDonneeDerogationEmployeDTO getEmploye() {
        return employe;
    }

    
    public void setEmploye(ProjectDemandeDataDonneeDerogationEmployeDTO value) {
        this.employe = value;
    }

    
    public String getMotivationdemande() {
        return motivationdemande;
    }

    
    public void setMotivationdemande(String value) {
        this.motivationdemande = value;
    }

    
    public OuinonEnum getPresencedeleguespersonnel() {
        return presencedeleguespersonnel;
    }

    
    public void setPresencedeleguespersonnel(OuinonEnum value) {
        this.presencedeleguespersonnel = value;
    }

    
    public String getNombresalarie() {
        return nombresalarie;
    }

    
    public void setNombresalarie(String value) {
        this.nombresalarie = value;
    }

}
