package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.shared.itg.resid.enums.ResidMotifDuplicataEnum;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeCertificatResidenceDTO implements Serializable {

    private static final long serialVersionUID = 7717984948087024605L;

    private ResidDemandeBaseDTO demandeBase;

    private String idTS;

    private String motif;

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

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}
