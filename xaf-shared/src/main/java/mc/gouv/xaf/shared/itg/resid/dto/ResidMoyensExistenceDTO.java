package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import mc.gouv.xaf.shared.itg.resid.enums.ResidSituationEnum;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidMoyensExistenceDTO implements Serializable {

    private static final long serialVersionUID = -5756654094218754527L;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ResidSituationEnum situationPrincipale;

    private String employeurRaisonSociale;

    private String employeurVille;

    private String employeurPays;

    public ResidSituationEnum getSituationPrincipale() {
        return situationPrincipale;
    }

    public void setSituationPrincipale(ResidSituationEnum situationPrincipale) {
        this.situationPrincipale = situationPrincipale;
    }

    public String getEmployeurRaisonSociale() {
        return employeurRaisonSociale;
    }

    public void setEmployeurRaisonSociale(String employeurRaisonSociale) {
        this.employeurRaisonSociale = employeurRaisonSociale;
    }

    public String getEmployeurVille() {
        return employeurVille;
    }

    public void setEmployeurVille(String employeurVille) {
        this.employeurVille = employeurVille;
    }

    public String getEmployeurPays() {
        return employeurPays;
    }

    public void setEmployeurPays(String employeurPays) {
        this.employeurPays = employeurPays;
    }
}
