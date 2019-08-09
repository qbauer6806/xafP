#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDeclarationDTO implements Serializable {

    private static final long serialVersionUID = 1563199701514L;
    private DeclarationTexteEnum acceptation;


    
    public DeclarationTexteEnum getAcceptation() {
        return acceptation;
    }

    
    public void setAcceptation(DeclarationTexteEnum value) {
        this.acceptation = value;
    }

}
