package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.shared.itg.resid.enums.ResidMotifRenouvellementEnum;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeRenouvellementCarteDTO implements Serializable {

    private static final long serialVersionUID = -200073602129896052L;

    private ResidDemandeBaseDTO demandeBase;

    private String idTS;

    private ResidMotifRenouvellementEnum motifRenouvellement;

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

    public ResidMotifRenouvellementEnum getMotifRenouvellement() {
        return motifRenouvellement;
    }

    public void setMotifRenouvellement(ResidMotifRenouvellementEnum motifRenouvellement) {
        this.motifRenouvellement = motifRenouvellement;
    }
}
