package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidEntiteDemandantCDREnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidTypeCanalDemandeEnum;

import java.io.Serializable;

@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeCertificatResidenceDTO implements Serializable {

    private static final long serialVersionUID = 7717984948087024605L;

    private ResidDemandeBaseDTO demandeBase;

    @Getter
    private String idTS;

    @Getter
    private ResidEntiteDemandantCDREnum entiteDemandantCDR;

    @Getter
    private ResidTypeCanalDemandeEnum typeCanalDemande;

    @Getter
    private ResidPaiementEnLigneDTO paiementEnLigne;

    @JsonProperty("demandeBaseV2")
    public ResidDemandeBaseDTO getDemandeBase() {
        return demandeBase;
    }

}
