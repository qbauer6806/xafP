package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidAdresseDTO implements Serializable {

    private static final long serialVersionUID = -4767579539536301033L;

    @JsonInclude()
    private String careOf;

    @JsonInclude()
    private String adresse1;

    private String adresse2;

    @JsonInclude()
    private String adresse3;

    private String adresse4;

    private String paysCode;

    @JsonInclude()
    private String bloc;

    @JsonInclude()
    private String etage;

    @JsonInclude()
    private String appartement;

}
