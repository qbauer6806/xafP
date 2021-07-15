package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import mc.gouv.xaf.back.dsp.enums.ResidPieceJustificativeTypeEnum;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidPieceJustificativeDTO implements Serializable {

    private static final long serialVersionUID = 9160763384281964658L;

    private ResidPieceJustificativeTypeEnum type;

    private String nomFichier;

    private String numero;

    private String dateDebutValidite;

    private String dateFinValidite;

    public ResidPieceJustificativeTypeEnum getType() {
        return type;
    }

    public void setType(ResidPieceJustificativeTypeEnum type) {
        this.type = type;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getDateDebutValidite() {
        return dateDebutValidite;
    }

    public void setDateDebutValidite(String dateDebutValidite) {
        this.dateDebutValidite = dateDebutValidite;
    }

    public String getDateFinValidite() {
        return dateFinValidite;
    }

    public void setDateFinValidite(String dateFinValidite) {
        this.dateFinValidite = dateFinValidite;
    }
}
