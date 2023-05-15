package mc.gouv.xaf.back.dsp.dto.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.ResidTypePieceIdentiteEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidNationaliteDTO implements Serializable {

	private static final long serialVersionUID = 8927813265707011626L;

	private String nationalite1;

	private ResidTypePieceIdentiteEnum typePiece;

	private String numeroPiece;

	private String dateDelivrance;

	private String dateFinValidite;

	private String paysDelivrance;

	private String nationalite2;

	private boolean ressortissant;

	public String getNationalite1() {
		return nationalite1;
	}

	public void setNationalite1(String nationalite1) {
		this.nationalite1 = nationalite1;
	}

	public ResidTypePieceIdentiteEnum getTypePiece() {
		return typePiece;
	}

	public void setTypePiece(ResidTypePieceIdentiteEnum typePiece) {
		this.typePiece = typePiece;
	}

	public String getNumeroPiece() {
		return numeroPiece;
	}

	public void setNumeroPiece(String numeroPiece) {
		this.numeroPiece = numeroPiece;
	}

	public String getDateDelivrance() {
		return dateDelivrance;
	}

	public void setDateDelivrance(String dateDelivrance) {
		this.dateDelivrance = dateDelivrance;
	}

	public String getDateFinValidite() {
		return dateFinValidite;
	}

	public void setDateFinValidite(String dateFinValidite) {
		this.dateFinValidite = dateFinValidite;
	}

	public String getPaysDelivrance() {
		return paysDelivrance;
	}

	public void setPaysDelivrance(String paysDelivrance) {
		this.paysDelivrance = paysDelivrance;
	}

	public String getNationalite2() {
		return nationalite2;
	}

	public void setNationalite2(String nationalite2) {
		this.nationalite2 = nationalite2;
	}

	public boolean isRessortissant() {
		return ressortissant;
	}

	public void setRessortissant(boolean ressortissant) {
		this.ressortissant = ressortissant;
	}

	@Override
	public String toString() {
		return "ResidNationaliteDTO{" + "nationalite1='" + nationalite1 + '\'' + ", typePiece='" + typePiece + '\''
				+ ", numeroPiece='" + numeroPiece + '\'' + ", dateDelivrance='" + dateDelivrance + '\''
				+ ", dateFinValidite='" + dateFinValidite + '\'' + ", paysDelivrance='" + paysDelivrance + '\''
				+ ", nationalite2='" + nationalite2 + '\'' + ", ressortissant='" + ressortissant + '}';
	}

}
