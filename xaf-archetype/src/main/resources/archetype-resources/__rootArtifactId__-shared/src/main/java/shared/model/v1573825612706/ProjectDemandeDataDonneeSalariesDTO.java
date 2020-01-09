#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1573825612706;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeSalariesDTO implements Serializable {

    private static final long serialVersionUID = 1573825612706L;
    private ProjectDemandeFieldDonneeSalariesTabsalariesitalieDTO[] tabsalariesitalie;
    private ProjectDemandeFieldDonneeSalariesTabsalariesfranceDTO[] tabsalariesfrance;
    private SaisirOuFichierEnum joindrefichier;


    
    public ProjectDemandeFieldDonneeSalariesTabsalariesitalieDTO[] getTabsalariesitalie() {
        return tabsalariesitalie;
    }

    
    public void setTabsalariesitalie(ProjectDemandeFieldDonneeSalariesTabsalariesitalieDTO[] value) {
        this.tabsalariesitalie = value;
    }

    
    public ProjectDemandeFieldDonneeSalariesTabsalariesfranceDTO[] getTabsalariesfrance() {
        return tabsalariesfrance;
    }

    
    public void setTabsalariesfrance(ProjectDemandeFieldDonneeSalariesTabsalariesfranceDTO[] value) {
        this.tabsalariesfrance = value;
    }

    
    public SaisirOuFichierEnum getJoindrefichier() {
        return joindrefichier;
    }

    
    public void setJoindrefichier(SaisirOuFichierEnum value) {
        this.joindrefichier = value;
    }

}
