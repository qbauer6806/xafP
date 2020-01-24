#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1573825612706;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ContenuProjectDemandeDTO implements Serializable {

    private static final long serialVersionUID = 1573825612706L;
    private ProjectDemandeDataDonneeDTO donnee;
    private String champvide;


    
    public ProjectDemandeDataDonneeDTO getDonnee() {
        return donnee;
    }

    
    public void setDonnee(ProjectDemandeDataDonneeDTO value) {
        this.donnee = value;
    }

    
    public String getChampvide() {
        return champvide;
    }

    
    public void setChampvide(String value) {
        this.champvide = value;
    }

}
