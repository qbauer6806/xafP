package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import mc.gouv.xaf.back.dsp.enums.ResidEntiteDemandantCDREnum;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeCertificatResidenceDTO implements Serializable {

    private static final long serialVersionUID = 7717984948087024605L;

    private ResidDemandeBaseDTO demandeBase;

    private String idTS;

    private ResidEntiteDemandantCDREnum entiteDemandantCDR;

    public ResidDemandeBaseDTO getDemandeBase() {
        return demandeBase;
    }

    public void setDemandeBase(ResidDemandeBaseDTO demandeBase) {
        this.demandeBase = demandeBase;
    }

    public String getIdTS() {
        return idTS;
    }

    public void setIdTS(String idTS) {
        this.idTS = idTS;
    }

    public ResidEntiteDemandantCDREnum getEntiteDemandantCDR() {
        return entiteDemandantCDR;
    }

    public void setEntiteDemandantCDR(ResidEntiteDemandantCDREnum entiteDemandantCDR) {
        this.entiteDemandantCDR = entiteDemandantCDR;
    }
}
