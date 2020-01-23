#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1573825612706;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeFieldDonneeSalariesTabsalariesitalieDTO implements Serializable {

    private static final long serialVersionUID = 1573825612706L;
    private String donneeSalariesTabsalariesitalieChampnom;
    private String donneeSalariesTabsalariesitalieChampprenom;
    private String donneeSalariesTabsalariesitalieChampqualif;
    private String donneeSalariesTabsalariesitalieChampdebutdetach;


    
    public String getDonneeSalariesTabsalariesitalieChampnom() {
        return donneeSalariesTabsalariesitalieChampnom;
    }

    
    public void setDonneeSalariesTabsalariesitalieChampnom(String value) {
        this.donneeSalariesTabsalariesitalieChampnom = value;
    }

    
    public String getDonneeSalariesTabsalariesitalieChampprenom() {
        return donneeSalariesTabsalariesitalieChampprenom;
    }

    
    public void setDonneeSalariesTabsalariesitalieChampprenom(String value) {
        this.donneeSalariesTabsalariesitalieChampprenom = value;
    }

    
    public String getDonneeSalariesTabsalariesitalieChampqualif() {
        return donneeSalariesTabsalariesitalieChampqualif;
    }

    
    public void setDonneeSalariesTabsalariesitalieChampqualif(String value) {
        this.donneeSalariesTabsalariesitalieChampqualif = value;
    }

    
    public String getDonneeSalariesTabsalariesitalieChampdebutdetach() {
        return donneeSalariesTabsalariesitalieChampdebutdetach;
    }

    
    public void setDonneeSalariesTabsalariesitalieChampdebutdetach(String value) {
        this.donneeSalariesTabsalariesitalieChampdebutdetach = value;
    }

}
