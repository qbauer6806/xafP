#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1573825612706;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeEntrepriseAdresseentrepriseDTO implements Serializable {

    private static final long serialVersionUID = 1573825612706L;
    private String ligne1;
    private String ligne2;
    private String ligne3;
    private String codePostal;
    private String ville;
    private String pays;


    
    public String getLigne1() {
        return ligne1;
    }

    
    public void setLigne1(String value) {
        this.ligne1 = value;
    }

    
    public String getLigne2() {
        return ligne2;
    }

    
    public void setLigne2(String value) {
        this.ligne2 = value;
    }

    
    public String getLigne3() {
        return ligne3;
    }

    
    public void setLigne3(String value) {
        this.ligne3 = value;
    }

    
    public String getCodePostal() {
        return codePostal;
    }

    
    public void setCodePostal(String value) {
        this.codePostal = value;
    }

    
    public String getVille() {
        return ville;
    }

    
    public void setVille(String value) {
        this.ville = value;
    }

    
    public String getPays() {
        return pays;
    }

    
    public void setPays(String value) {
        this.pays = value;
    }

}
