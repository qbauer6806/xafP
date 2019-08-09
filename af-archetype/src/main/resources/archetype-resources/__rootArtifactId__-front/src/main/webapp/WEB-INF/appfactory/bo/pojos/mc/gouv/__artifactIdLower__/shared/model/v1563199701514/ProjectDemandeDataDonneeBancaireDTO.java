#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeBancaireDTO implements Serializable {

    private static final long serialVersionUID = 1563199701514L;
    private String nometsbancaire;
    private ProjectDemandeDataDonneeBancaireRibDTO rib;


    
    public String getNometsbancaire() {
        return nometsbancaire;
    }

    
    public void setNometsbancaire(String value) {
        this.nometsbancaire = value;
    }

    
    public ProjectDemandeDataDonneeBancaireRibDTO getRib() {
        return rib;
    }

    
    public void setRib(ProjectDemandeDataDonneeBancaireRibDTO value) {
        this.rib = value;
    }

}
