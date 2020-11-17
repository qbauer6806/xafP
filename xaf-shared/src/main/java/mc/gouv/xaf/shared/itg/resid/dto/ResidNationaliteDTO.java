package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.shared.itg.resid.enums.ResidPieceJustificativeTypeEnum;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidNationaliteDTO implements Serializable {

    private static final long serialVersionUID = -7625177412815882902L;

    private String nationaliteCode;

    private ResidPieceJustificativeTypeEnum pieceType;

    private String pieceNumero;

    private String pieceDateDelivrance;

    private String pieceDateFinValidite;

    private String piecePaysDelivrance;

    public String getNationaliteCode() {
        return nationaliteCode;
    }

    public void setNationaliteCode(String nationaliteCode) {
        this.nationaliteCode = nationaliteCode;
    }

    public ResidPieceJustificativeTypeEnum getPieceType() {
        return pieceType;
    }

    public void setPieceType(ResidPieceJustificativeTypeEnum pieceType) {
        this.pieceType = pieceType;
    }

    public String getPieceNumero() {
        return pieceNumero;
    }

    public void setPieceNumero(String pieceNumero) {
        this.pieceNumero = pieceNumero;
    }

    public String getPieceDateDelivrance() {
        return pieceDateDelivrance;
    }

    public void setPieceDateDelivrance(String pieceDateDelivrance) {
        this.pieceDateDelivrance = pieceDateDelivrance;
    }

    public String getPieceDateFinValidite() {
        return pieceDateFinValidite;
    }

    public void setPieceDateFinValidite(String pieceDateFinValidite) {
        this.pieceDateFinValidite = pieceDateFinValidite;
    }

    public String getPiecePaysDelivrance() {
        return piecePaysDelivrance;
    }

    public void setPiecePaysDelivrance(String piecePaysDelivrance) {
        this.piecePaysDelivrance = piecePaysDelivrance;
    }
}
