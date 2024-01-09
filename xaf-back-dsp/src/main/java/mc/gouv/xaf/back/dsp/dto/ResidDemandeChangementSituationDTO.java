package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import mc.gouv.xaf.back.dsp.enums.common.ResidMotifChangementSituationEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeChangementSituationDTO implements Serializable {

    private static final long serialVersionUID = -241685857915542197L;

    private ResidDemandeBaseDTO demandeBase;

    private String idTS;

    private ResidMotifChangementSituationEnum motifChangementSituation;

    @JsonProperty("demandeBaseV2")
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
