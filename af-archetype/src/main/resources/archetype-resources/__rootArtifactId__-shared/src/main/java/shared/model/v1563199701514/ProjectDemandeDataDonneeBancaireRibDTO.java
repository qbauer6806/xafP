#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeBancaireRibDTO implements Serializable {

    private static final long serialVersionUID = 1563199701514L;
    private String titulaire;
    private String bic;
    private String iban;


    
    public String getTitulaire() {
        return titulaire;
    }

    
    public void setTitulaire(String value) {
        this.titulaire = value;
    }

    
    public String getBic() {
        return bic;
    }

    
    public void setBic(String value) {
        this.bic = value;
    }

    
    public String getIban() {
        return iban;
    }

    
    public void setIban(String value) {
        this.iban = value;
    }

}
