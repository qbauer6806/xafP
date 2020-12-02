package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.shared.itg.resid.enums.ResidSituationEnum;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidMoyensExistenceDTO implements Serializable {

    private static final long serialVersionUID = -5756654094218754527L;

    private ResidSituationEnum situationPrincipale;

    private String banqueDate;

    private String revenuDateJustificatif;

    private String revenuPeriodicite;

    private String revenuTypeJustificatif;

    private String employeurRaisonSociale;

    public ResidSituationEnum getSituationPrincipale() {
        return situationPrincipale;
    }

    public void setSituationPrincipale(ResidSituationEnum situationPrincipale) {
        this.situationPrincipale = situationPrincipale;
    }

    public String getBanqueDate() {
        return banqueDate;
    }

    public void setBanqueDate(String banqueDate) {
        this.banqueDate = banqueDate;
    }

    public String getRevenuDateJustificatif() {
        return revenuDateJustificatif;
    }

    public void setRevenuDateJustificatif(String revenuDateJustificatif) {
        this.revenuDateJustificatif = revenuDateJustificatif;
    }

    public String getRevenuPeriodicite() {
        return revenuPeriodicite;
    }

    public void setRevenuPeriodicite(String revenuPeriodicite) {
        this.revenuPeriodicite = revenuPeriodicite;
    }

    public String getRevenuTypeJustificatif() {
        return revenuTypeJustificatif;
    }

    public void setRevenuTypeJustificatif(String revenuTypeJustificatif) {
        this.revenuTypeJustificatif = revenuTypeJustificatif;
    }

    public String getEmployeurRaisonSociale() {
        return employeurRaisonSociale;
    }

    public void setEmployeurRaisonSociale(String employeurRaisonSociale) {
        this.employeurRaisonSociale = employeurRaisonSociale;
    }
}
