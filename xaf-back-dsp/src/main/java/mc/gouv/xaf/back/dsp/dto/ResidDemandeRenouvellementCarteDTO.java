package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import mc.gouv.xaf.back.dsp.enums.ResidMotifRenouvellementEnum;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
