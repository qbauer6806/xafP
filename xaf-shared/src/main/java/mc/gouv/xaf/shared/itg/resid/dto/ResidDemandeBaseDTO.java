package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.shared.itg.resid.enums.ResidDemandeurTypeEnum;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeBaseDTO implements Serializable {

    private static final long serialVersionUID = -1342596710274307110L;

    private String date;

    private ResidDemandeurTypeEnum demandeur;

    private String demandeurNom;

    private List<ResidPieceJustificativeDTO> piecesJustificatives;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ResidDemandeurTypeEnum getDemandeur() {
        return demandeur;
    }

    public void setDemandeur(ResidDemandeurTypeEnum demandeur) {
        this.demandeur = demandeur;
    }

    public String getDemandeurNom() {
        return demandeurNom;
    }

    public void setDemandeurNom(String demandeurNom) {
        this.demandeurNom = demandeurNom;
    }

    public List<ResidPieceJustificativeDTO> getPiecesJustificatives() {
        return piecesJustificatives;
    }

    public void setPiecesJustificatives(List<ResidPieceJustificativeDTO> piecesJustificatives) {
        this.piecesJustificatives = piecesJustificatives;
    }
}
