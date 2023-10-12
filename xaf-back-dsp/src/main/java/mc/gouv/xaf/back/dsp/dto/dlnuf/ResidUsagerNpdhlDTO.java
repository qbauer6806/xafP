package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.dto.ResidAdresseDTO;
import mc.gouv.xaf.back.dsp.dto.ResidMoyensExistenceDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidUsagerNpdhlDTO implements Serializable {

    private static final long serialVersionUID = 1151801717747924423L;
    
    private ResidIdentiteDLN1FDTO identite;
    
	private ResidContactDNL1FDTO contacts;
    
    private ResidResidenceDLN1FDTO residence;
    
    private ResidMoyensExistenceDTO moyenExistence;
    
    private ResidAdresseDTO adresse;
    
    private ResidNationalite1Et2DTO nationalite;
    
    private ResidResidentDLN1FDTO resident;
    
    private ResidSituationFamilialeDLN1FDTO situationFamiliale;
    
    private ResidEnfantsDLN1FDTO enfants;
    
    private ResidMembresFoyerDLN1FDTO membresFoyer;
    
    public ResidIdentiteDLN1FDTO getIdentite() {
		return identite;
	}

	public void setIdentite(ResidIdentiteDLN1FDTO identite) {
		this.identite = identite;
	}

	public ResidContactDNL1FDTO getContacts() {
		return contacts;
	}

	public void setContacts(ResidContactDNL1FDTO contacts) {
		this.contacts = contacts;
	}

	public ResidResidenceDLN1FDTO getResidence() {
		return residence;
	}

	public void setResidence(ResidResidenceDLN1FDTO residence) {
		this.residence = residence;
	}

	public ResidMoyensExistenceDTO getMoyenExistence() {
		return moyenExistence;
	}

	public void setMoyenExistence(ResidMoyensExistenceDTO moyenExistence) {
		this.moyenExistence = moyenExistence;
	}

	public ResidAdresseDTO getAdresse() {
		return adresse;
	}

	public void setAdresse(ResidAdresseDTO adresse) {
		this.adresse = adresse;
	}

	public ResidNationalite1Et2DTO getNationalite() {
		return nationalite;
	}

	public void setNationalite(ResidNationalite1Et2DTO nationalite) {
		this.nationalite = nationalite;
	}

	public ResidResidentDLN1FDTO getResident() {
		return resident;
	}

	public void setResident(ResidResidentDLN1FDTO resident) {
		this.resident = resident;
	}

	public ResidSituationFamilialeDLN1FDTO getSituationFamiliale() {
		return situationFamiliale;
	}

	public void setSituationFamiliale(ResidSituationFamilialeDLN1FDTO situationFamiliale) {
		this.situationFamiliale = situationFamiliale;
	}

	public ResidEnfantsDLN1FDTO getEnfants() {
		return enfants;
	}

	public void setEnfants(ResidEnfantsDLN1FDTO enfants) {
		this.enfants = enfants;
	}

    @Override
    public String toString() {
        return "ResidResidentCorrespondanceDTO{" +
                "identite='" + identite.toString() + '\'' +
                ", contacts='" + contacts.toString() + '\'' +
                ", residence='" + residence.toString() + '\'' +
                ", moyenExistence='" + moyenExistence.toString() + '\'' +
                ", adresse='" + adresse.toString() + '\'' +
                ", nationalite='" + nationalite.toString() + '\'' +
                ", resident='" + resident.toString() + '\'' +
                ", situationFamiliale='" + situationFamiliale.toString() + '\'' +
                ", enfants=" + enfants.toString() +
                '}';
    }

	public ResidMembresFoyerDLN1FDTO getMembresFoyer() {
		return membresFoyer;
	}

	public void setMembresFoyer(ResidMembresFoyerDLN1FDTO membresFoyer) {
		this.membresFoyer = membresFoyer;
	}
}
