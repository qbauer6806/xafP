package mc.gouv.xaf.back.dsp.dto.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidResidenceDTO implements Serializable {
	
	private static final long serialVersionUID = -1720606882440326053L;

	private boolean locationLogement;
	
	private Integer nombreOccupant;

	private Integer loyer;

	private Integer nombrePiece;

	private Integer nombreStationnement;
	
	private Integer surfaceM2;
	
	private ResidQualiteDTO qualite;
	
	private ResidLoyerPeriodiciteDTO loyerPeriodicite;
	

	public Integer getNombreOccupant() {
		return nombreOccupant;
	}

	public void setNombreOccupant(Integer nombreOccupant) {
		this.nombreOccupant = nombreOccupant;
	}

	public Integer getLoyer() {
		return loyer;
	}

	public void setLoyer(Integer loyer) {
		this.loyer = loyer;
	}

	public Integer getNombrePiece() {
		return nombrePiece;
	}

	public void setNombrePiece(Integer nombrePiece) {
		this.nombrePiece = nombrePiece;
	}

	public Integer getNombreStationnement() {
		return nombreStationnement;
	}

	public void setNombreStationnement(Integer nombreStationnement) {
		this.nombreStationnement = nombreStationnement;
	}

	public Integer getSurfaceM2() {
		return surfaceM2;
	}

	public void setSurfaceM2(Integer surfaceM2) {
		this.surfaceM2 = surfaceM2;
	}
	
	public boolean isLocationLogement() {
		return locationLogement;
	}

	public void setLocationLogement(boolean locationLogement) {
		this.locationLogement = locationLogement;
	}

	public ResidQualiteDTO getQualite() {
		return qualite;
	}

	public void setQualite(ResidQualiteDTO qualite) {
		this.qualite = qualite;
	}

	public ResidLoyerPeriodiciteDTO getLoyerPeriodicite() {
		return loyerPeriodicite;
	}

	public void setLoyerPeriodicite(ResidLoyerPeriodiciteDTO loyerPeriodicite) {
		this.loyerPeriodicite = loyerPeriodicite;
	}

	@Override
	public String toString() {
		return "ResidResidenceDTO{" + "qualite='" + qualite.toString() + '\'' + ", locationLogement='"
				+ locationLogement + '\'' + ", nombreOccupant='" + nombreOccupant + '\'' + ", loyer='" + loyer + '\''
				+ ", nombrePiece='" + nombrePiece + '\'' + ", nombreStationnement='" + nombreStationnement + '\''
				+ ", surfaceM2='" + surfaceM2 + '\'' + ", loyerPeriodicite='" + loyerPeriodicite.toString() + '}';
	}

}
