package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidLoyerPeriodiciteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidQualiteEnum;

import java.io.Serializable;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidLogementDTO implements Serializable {

    private static final long serialVersionUID = -7593359863350045615L;

    private ResidQualiteEnum occupantQualite;

    private int nombrePieces;

    private Integer surface;

    private Integer loyer;

    private ResidLoyerPeriodiciteEnum loyerPeriodicite;

    @JsonInclude()
    private String dateDerniereQuittance;

    private int nombreStationnements;

    private int nombreOccupants;

}
