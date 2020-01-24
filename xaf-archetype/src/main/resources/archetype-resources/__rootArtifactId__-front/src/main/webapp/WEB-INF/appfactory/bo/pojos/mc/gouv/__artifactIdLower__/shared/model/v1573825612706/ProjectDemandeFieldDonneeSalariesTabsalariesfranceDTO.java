#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1573825612706;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeFieldDonneeSalariesTabsalariesfranceDTO implements Serializable {

    private static final long serialVersionUID = 1573825612706L;
    private String donneeSalariesTabsalariesfranceChampnom;
    private String donneeSalariesTabsalariesfranceChampprenom;
    private String donneeSalariesTabsalariesfranceChampnumsecu;
    private String donneeSalariesTabsalariesfranceChampqualif;
    private String donneeSalariesTabsalariesfranceChampdebutdetach;


    
    public String getDonneeSalariesTabsalariesfranceChampnom() {
        return donneeSalariesTabsalariesfranceChampnom;
    }

    
    public void setDonneeSalariesTabsalariesfranceChampnom(String value) {
        this.donneeSalariesTabsalariesfranceChampnom = value;
    }

    
    public String getDonneeSalariesTabsalariesfranceChampprenom() {
        return donneeSalariesTabsalariesfranceChampprenom;
    }

    
    public void setDonneeSalariesTabsalariesfranceChampprenom(String value) {
        this.donneeSalariesTabsalariesfranceChampprenom = value;
    }

    
    public String getDonneeSalariesTabsalariesfranceChampnumsecu() {
        return donneeSalariesTabsalariesfranceChampnumsecu;
    }

    
    public void setDonneeSalariesTabsalariesfranceChampnumsecu(String value) {
        this.donneeSalariesTabsalariesfranceChampnumsecu = value;
    }

    
    public String getDonneeSalariesTabsalariesfranceChampqualif() {
        return donneeSalariesTabsalariesfranceChampqualif;
    }

    
    public void setDonneeSalariesTabsalariesfranceChampqualif(String value) {
        this.donneeSalariesTabsalariesfranceChampqualif = value;
    }

    
    public String getDonneeSalariesTabsalariesfranceChampdebutdetach() {
        return donneeSalariesTabsalariesfranceChampdebutdetach;
    }

    
    public void setDonneeSalariesTabsalariesfranceChampdebutdetach(String value) {
        this.donneeSalariesTabsalariesfranceChampdebutdetach = value;
    }

}
