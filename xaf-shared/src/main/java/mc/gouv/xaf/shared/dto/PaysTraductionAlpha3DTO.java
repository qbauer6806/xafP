package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaysTraductionAlpha3DTO {
    
    private String alpha2;

    private String alpha3;

}
