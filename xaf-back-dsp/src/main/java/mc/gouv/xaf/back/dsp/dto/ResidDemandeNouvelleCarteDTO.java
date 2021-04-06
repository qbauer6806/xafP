package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import mc.gouv.xaf.back.dsp.enums.ResidCarteTypeEnum;
import mc.gouv.xaf.back.dsp.enums.ResidMotifInstallationEnum;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeNouvelleCarteDTO implements Serializable {

    private static final long serialVersionUID = -4050102543540340918L;

    private ResidDemandeBaseDTO demandeBase;

    private String idTS;

    private ResidCarteTypeEnum type;

    private ResidMotifInstallationEnum motifInstallation;

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

    public ResidCarteTypeEnum getType() {
        return type;
    }

    public void setType(ResidCarteTypeEnum type) {
        this.type = type;
    }

    public ResidMotifInstallationEnum getMotifInstallation() {
        return motifInstallation;
    }

    public void setMotifInstallation(ResidMotifInstallationEnum motifInstallation) {
        this.motifInstallation = motifInstallation;
    }
}
