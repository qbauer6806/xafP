#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1573825612706;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataDonneeEntrepriseDTO implements Serializable {

    private static final long serialVersionUID = 1573825612706L;
    private PaysOrigineDetachementEnum paysoriginedetachement;
    private String datesdetachement;
    private OuinonEnum estchantier;
    private String nomchantier;
    private String nomentreprise;
    private String nautorisationchantier;
    private String datedebutchantier;
    private String datefinchantier;
    private ProjectDemandeDataDonneeEntrepriseAdressechantierDTO adressechantier;
    private ProjectDemandeDataDonneeEntrepriseAdresseentrepriseDTO adresseentreprise;


    
    public PaysOrigineDetachementEnum getPaysoriginedetachement() {
        return paysoriginedetachement;
    }

    
    public void setPaysoriginedetachement(PaysOrigineDetachementEnum value) {
        this.paysoriginedetachement = value;
    }

    
    public String getDatesdetachement() {
        return datesdetachement;
    }

    
    public void setDatesdetachement(String value) {
        this.datesdetachement = value;
    }

    
    public OuinonEnum getEstchantier() {
        return estchantier;
    }

    
    public void setEstchantier(OuinonEnum value) {
        this.estchantier = value;
    }

    
    public String getNomchantier() {
        return nomchantier;
    }

    
    public void setNomchantier(String value) {
        this.nomchantier = value;
    }

    
    public String getNomentreprise() {
        return nomentreprise;
    }

    
    public void setNomentreprise(String value) {
        this.nomentreprise = value;
    }

    
    public String getNautorisationchantier() {
        return nautorisationchantier;
    }

    
    public void setNautorisationchantier(String value) {
        this.nautorisationchantier = value;
    }

    
    public String getDatedebutchantier() {
        return datedebutchantier;
    }

    
    public void setDatedebutchantier(String value) {
        this.datedebutchantier = value;
    }

    
    public String getDatefinchantier() {
        return datefinchantier;
    }

    
    public void setDatefinchantier(String value) {
        this.datefinchantier = value;
    }

    
    public ProjectDemandeDataDonneeEntrepriseAdressechantierDTO getAdressechantier() {
        return adressechantier;
    }

    
    public void setAdressechantier(ProjectDemandeDataDonneeEntrepriseAdressechantierDTO value) {
        this.adressechantier = value;
    }

    
    public ProjectDemandeDataDonneeEntrepriseAdresseentrepriseDTO getAdresseentreprise() {
        return adresseentreprise;
    }

    
    public void setAdresseentreprise(ProjectDemandeDataDonneeEntrepriseAdresseentrepriseDTO value) {
        this.adresseentreprise = value;
    }

}
