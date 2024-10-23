package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.dto.ResidEnfantDTO;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidEnfantsDLN1FDTO implements Serializable {

    private static final long serialVersionUID = 4037812219398491343L;

    private boolean hasEnfantMineur;

    private Integer nombreEnfantsMineur;

    private List<ResidEnfantDTO> enfants;

}
