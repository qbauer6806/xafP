package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidIdentificationTypeEnum;

import java.io.Serializable;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidResidentDTO implements Serializable {

    private static final long serialVersionUID = -2065671673609703252L;

    private ResidIdentificationTypeEnum identificationType;

    private String numeroResident;

    private String numeroCarte;

}
