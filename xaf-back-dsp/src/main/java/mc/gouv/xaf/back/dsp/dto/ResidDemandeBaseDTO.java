package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidDemandeurTypeEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidDemandeBaseDTO implements Serializable {

    private static final long serialVersionUID = -1342596710274307110L;

    private String date;

    private ResidDemandeurTypeEnum demandeur;

    private String demandeurNom;

    private String enqueteurMatricule;

    private String administrateurNom;

    private List<ResidPieceJustificativeDTO> piecesJustificatives;
}
