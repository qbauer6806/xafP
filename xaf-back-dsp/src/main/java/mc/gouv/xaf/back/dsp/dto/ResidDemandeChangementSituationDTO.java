package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidMotifChangementSituationEnum;

@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeChangementSituationDTO implements Serializable {

    private static final long serialVersionUID = -241685857915542197L;

    private ResidDemandeBaseDTO demandeBase;

    @Getter
    private String idTS;

    @Getter
    private ResidMotifChangementSituationEnum motifChangementSituation;

    @JsonProperty("demandeBaseV2")
    public ResidDemandeBaseDTO getDemandeBase() {
        return demandeBase;
    }

}
