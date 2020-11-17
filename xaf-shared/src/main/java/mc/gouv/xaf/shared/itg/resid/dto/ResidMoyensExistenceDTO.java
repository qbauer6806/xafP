package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.shared.itg.resid.enums.ResidSituationEnum;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidMoyensExistenceDTO implements Serializable {

    private static final long serialVersionUID = -5756654094218754527L;

    private ResidSituationEnum situationPrincipale;

    public ResidSituationEnum getSituationPrincipale() {
        return situationPrincipale;
    }

    public void setSituationPrincipale(ResidSituationEnum situationPrincipale) {
        this.situationPrincipale = situationPrincipale;
    }
}
