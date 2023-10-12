package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.common.ResidLoyerPeriodiciteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidQualiteEnum;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidLogementDTO implements Serializable {

    private static final long serialVersionUID = -7593359863350045615L;

    private ResidQualiteEnum occupantQualite;

    private int nombrePieces;

    @JsonInclude()
    private Integer surface;

    private int loyer;

    private ResidLoyerPeriodiciteEnum loyerPeriodicite;

    @JsonInclude()
    private String dateDerniereQuittance;

    private int nombreStationnements;

    private int nombreOccupants;

    public ResidQualiteEnum getOccupantQualite() {
        return occupantQualite;
    }

    public void setOccupantQualite(ResidQualiteEnum occupantQualite) {
        this.occupantQualite = occupantQualite;
    }

    public Integer getSurface() {
        return surface;
    }

    public void setSurface(Integer surface) {
        this.surface = surface;
    }

    public ResidLoyerPeriodiciteEnum getLoyerPeriodicite() {
        return loyerPeriodicite;
    }

    public void setLoyerPeriodicite(ResidLoyerPeriodiciteEnum loyerPeriodicite) {
        this.loyerPeriodicite = loyerPeriodicite;
    }

    public String getDateDerniereQuittance() {
        return dateDerniereQuittance;
    }

    public void setDateDerniereQuittance(String dateDerniereQuittance) {
        this.dateDerniereQuittance = dateDerniereQuittance;
    }

    public int getNombrePieces() {
        return nombrePieces;
    }

    public void setNombrePieces(int nombrePieces) {
        this.nombrePieces = nombrePieces;
    }

    public int getLoyer() {
        return loyer;
    }

    public void setLoyer(int loyer) {
        this.loyer = loyer;
    }

    public int getNombreStationnements() {
        return nombreStationnements;
    }

    public void setNombreStationnements(int nombreStationnements) {
        this.nombreStationnements = nombreStationnements;
    }

    public int getNombreOccupants() {
        return nombreOccupants;
    }

    public void setNombreOccupants(int nombreOccupants) {
        this.nombreOccupants = nombreOccupants;
    }
}
