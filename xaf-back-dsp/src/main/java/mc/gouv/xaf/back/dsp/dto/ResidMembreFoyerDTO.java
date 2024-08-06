package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidMembreRelationEnum;

import java.io.Serializable;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidMembreFoyerDTO implements Serializable {

    private static final long serialVersionUID = 5659961517651205985L;

    private ResidCiviliteEnum membreCivilite;

    private String membreNom;

    private String membrePrenom;

    private String membreDateNaissance;

    private String membreNationaliteCode;

    private ResidMembreRelationEnum membreRelation;

    private boolean membreFoyer;

}
