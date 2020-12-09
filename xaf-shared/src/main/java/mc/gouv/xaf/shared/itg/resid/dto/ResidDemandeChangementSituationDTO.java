package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.shared.itg.resid.enums.ResidMotifChangementSituationEnum;
import mc.gouv.xaf.shared.itg.resid.enums.ResidMotifDuplicataEnum;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeChangementSituationDTO implements Serializable {

    private static final long serialVersionUID = -241685857915542197L;

    private ResidDemandeBaseDTO demandeBase;

    private String idTS;

    private ResidMotifChangementSituationEnum motifChangementSituation;

    public ResidDemandeBaseDTO getDemandeBase() {
        return demandeBase;
    }

    public void setDemandeBase(ResidDemandeBaseDTO demandeBase) {
        this.demandeBase = demandeBase;
    }

    public String getIdTS() {
        return idTS;
    }

    public void setIdTS(String idTS) {
        this.idTS = idTS;
    }

    public ResidMotifChangementSituationEnum getMotifChangementSituation() {
        return motifChangementSituation;
    }

    public void setMotifChangementSituation(ResidMotifChangementSituationEnum motifChangementSituation) {
        this.motifChangementSituation = motifChangementSituation;
    }
}
