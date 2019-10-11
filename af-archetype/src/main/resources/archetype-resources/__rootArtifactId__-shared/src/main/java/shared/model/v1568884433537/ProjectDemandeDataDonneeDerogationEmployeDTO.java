#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1568884433537;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeDerogationEmployeDTO implements Serializable {

    private static final long serialVersionUID = 1568884433537L;
    private String concerne;


    
    public String getConcerne() {
        return concerne;
    }

    
    public void setConcerne(String value) {
        this.concerne = value;
    }

}
