package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidStatutCaisseEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidStatutCaisseDTO {

    @JsonInclude()
    private ResidStatutCaisseEnum action;
}
