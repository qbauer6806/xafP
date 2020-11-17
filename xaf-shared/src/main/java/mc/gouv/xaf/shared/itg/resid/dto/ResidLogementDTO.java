package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.shared.itg.resid.enums.ResidLoyerPeriodiciteEnum;
import mc.gouv.xaf.shared.itg.resid.enums.ResidQualiteEnum;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidLogementDTO implements Serializable {

    private static final long serialVersionUID = -7593359863350045615L;

    private ResidQualiteEnum occupantQualite;

    private Integer nombrePieces;

    private Integer surface;

    private Integer loyer;

    private ResidLoyerPeriodiciteEnum loyerPeriodicite;

    private String dateDerniereQuittance;

    private Integer nombreStationnements;

    private Integer nombreOccupants;

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

    public Integer getLoyer() {
        return loyer;
    }

    public void setLoyer(Integer loyer) {
        this.loyer = loyer;
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

    public Integer getNombreStationnements() {
        return nombreStationnements;
    }

    public void setNombreStationnements(Integer nombreStationnements) {
        this.nombreStationnements = nombreStationnements;
    }

    public Integer getNombreOccupants() {
        return nombreOccupants;
    }

    public void setNombreOccupants(Integer nombreOccupants) {
        this.nombreOccupants = nombreOccupants;
    }

    public Integer getNombrePieces() {
        return nombrePieces;
    }

    public void setNombrePieces(Integer nombrePieces) {
        this.nombrePieces = nombrePieces;
    }
}
