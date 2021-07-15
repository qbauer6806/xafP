package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import mc.gouv.xaf.back.dsp.enums.ResidMotifChangementSituationEnum;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
