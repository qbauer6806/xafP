#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InformationsDetachementDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8621310861790358330L;

    private Integer nombreSalariesDetaches;

    private Integer nombreSalariesNonDetaches;

    public Integer getNombreSalariesDetaches() {
        return nombreSalariesDetaches;
    }

    public void setNombreSalariesDetaches(Integer nombreSalariesDetaches) {
        this.nombreSalariesDetaches = nombreSalariesDetaches;
    }

    public Integer getNombreSalariesNonDetaches() {
        return nombreSalariesNonDetaches;
    }

    public void setNombreSalariesNonDetaches(Integer nombreSalariesNonDetaches) {
        this.nombreSalariesNonDetaches = nombreSalariesNonDetaches;
    }
}
