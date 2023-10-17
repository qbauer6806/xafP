package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import mc.gouv.xaf.back.dsp.enums.common.ResidMotifDuplicataEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeDuplicataCarteDTO implements Serializable {

    private static final long serialVersionUID = 7717984948087024605L;

    private ResidDemandeBaseDTO demandeBase;

    private String idTS;

    private ResidMotifDuplicataEnum motifDuplicata;

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

    public ResidMotifDuplicataEnum getMotifDuplicata() {
        return motifDuplicata;
    }

    public void setMotifDuplicata(ResidMotifDuplicataEnum motifDuplicata) {
        this.motifDuplicata = motifDuplicata;
    }
}
