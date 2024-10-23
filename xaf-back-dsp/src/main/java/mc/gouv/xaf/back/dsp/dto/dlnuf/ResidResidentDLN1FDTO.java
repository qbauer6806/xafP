package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidTypeCarteMroadEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidResidentDLN1FDTO implements Serializable {

    private static final long serialVersionUID = 3142537019985412980L;

    private String numeroCarte;

    private String dateDebutValidite;

    private String dateFinValidite;

    private ResidTypeCarteMroadEnum type;

    private String dateEtablissementMonaco;

}
