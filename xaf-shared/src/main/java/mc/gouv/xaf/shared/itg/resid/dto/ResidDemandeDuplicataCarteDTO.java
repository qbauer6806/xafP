package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.shared.itg.resid.enums.ResidMotifDuplicataEnum;
import mc.gouv.xaf.shared.itg.resid.enums.ResidMotifRenouvellementEnum;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeDuplicataCarteDTO implements Serializable {

    private static final long serialVersionUID = 7717984948087024605L;

    private ResidDemandeBaseDTO demandeBase;

    private String idTS;

    private ResidMotifDuplicataEnum motifDuplicata;

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

    public ResidMotifDuplicataEnum getMotifDuplicata() {
        return motifDuplicata;
    }

    public void setMotifDuplicata(ResidMotifDuplicataEnum motifDuplicata) {
        this.motifDuplicata = motifDuplicata;
    }
}
