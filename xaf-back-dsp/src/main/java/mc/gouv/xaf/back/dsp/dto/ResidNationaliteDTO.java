package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidPieceJustificativeTypeEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidNationaliteDTO implements Serializable {

    private static final long serialVersionUID = -7625177412815882902L;

    private String nationaliteCode;

    private ResidPieceJustificativeTypeEnum pieceType;

    private String pieceNumero;

    private String pieceDateDelivrance;

    private String pieceDateFinValidite;

    private String piecePaysDelivrance;

}
