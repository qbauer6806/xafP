#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1568884433537;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ContenuProjectDemandeDTO implements Serializable {

    private static final long serialVersionUID = 1568884433537L;
    private ProjectDemandeDataDonneeDTO donnee;


    
    public ProjectDemandeDataDonneeDTO getDonnee() {
        return donnee;
    }

    
    public void setDonnee(ProjectDemandeDataDonneeDTO value) {
        this.donnee = value;
    }

}
