package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidCarteTypeEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidMotifInstallationEnum;

import java.io.Serializable;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeNouvelleCarteDTO implements Serializable {

    private static final long serialVersionUID = -4050102543540340918L;

    @JsonProperty("demandeBaseV2")
    private ResidDemandeBaseDTO demandeBase;

    private String idTS;

    private ResidCarteTypeEnum type;

    private ResidMotifInstallationEnum motifInstallation;

}
