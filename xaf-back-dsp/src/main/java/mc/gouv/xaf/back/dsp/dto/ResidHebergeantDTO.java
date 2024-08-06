package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidQualiteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidSexeEnum;
import mc.gouv.xaf.back.dsp.enums.v2.ResidRelationEnum;

import java.io.Serializable;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidHebergeantDTO implements Serializable {

    private static final long serialVersionUID = -2474143139113858294L;

    private ResidCiviliteEnum hebergeantCivilite;

    private String hebergeantNomRaisonSociale;

    @JsonInclude()
    private String hebergeantNomUsage;

    @JsonInclude()
    private String hebergeantPrenom;

    private ResidSexeEnum hebergeantSexe;

    @JsonInclude()
    private String hebergantNationaliteCode;

    private ResidQualiteEnum hebegeantQualite;

    private ResidRelationEnum hebergeantRelation;

}
