package mc.gouv.xaf.back.dsp.dto.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidResidentCorrespondanceDTO implements Serializable {

    private static final long serialVersionUID = 1151801717747924423L;
    
    private ResidIdentiteDTO identite;
    
	private ResidContactsDTO contacts;
    
    private ResidResidenceDTO residence;
    
    private ResidMoyenExistenceDTO moyenExistence;
    
    private ResidAdresseDTO adresse;
    
    private ResidNationaliteDTO nationalite;
    
    private ResidResidentDTO resident;
    
    private ResidSituationFamilialeDTO situationFamiliale;
    
    private ResidEnfantsDTO enfants;
    
    public ResidIdentiteDTO getIdentite() {
		return identite;
	}

	public void setIdentite(ResidIdentiteDTO identite) {
		this.identite = identite;
	}

	public ResidContactsDTO getContacts() {
		return contacts;
	}

	public void setContacts(ResidContactsDTO contacts) {
		this.contacts = contacts;
	}

	public ResidResidenceDTO getResidence() {
		return residence;
	}

	public void setResidence(ResidResidenceDTO residence) {
		this.residence = residence;
	}

	public ResidMoyenExistenceDTO getMoyenExistence() {
		return moyenExistence;
	}

	public void setMoyenExistence(ResidMoyenExistenceDTO moyenExistence) {
		this.moyenExistence = moyenExistence;
	}

	public ResidAdresseDTO getAdresse() {
		return adresse;
	}

	public void setAdresse(ResidAdresseDTO adresse) {
		this.adresse = adresse;
	}

	public ResidNationaliteDTO getNationalite() {
		return nationalite;
	}

	public void setNationalite(ResidNationaliteDTO nationalite) {
		this.nationalite = nationalite;
	}

	public ResidResidentDTO getResident() {
		return resident;
	}

	public void setResident(ResidResidentDTO resident) {
		this.resident = resident;
	}

	public ResidSituationFamilialeDTO getSituationFamiliale() {
		return situationFamiliale;
	}

	public void setSituationFamiliale(ResidSituationFamilialeDTO situationFamiliale) {
		this.situationFamiliale = situationFamiliale;
	}

	public ResidEnfantsDTO getEnfants() {
		return enfants;
	}

	public void setEnfants(ResidEnfantsDTO enfants) {
		this.enfants = enfants;
	}

    @Override
    public String toString() {
        return "ResidResidentCorrespondanceDTO{" +
                "numeroUsager='" + numeroUsager + '\'' +
                ", nom='" + nom + '\'' +
                ", nomUsage='" + nomUsage + '\'' +
                ", prenom='" + prenom + '\'' +
                ", dateNaissance='" + dateNaissance + '\'' +
                ", villeNaissance='" + villeNaissance + '\'' +
                ", paysNaissanceCode='" + paysNaissanceCode + '\'' +
                ", paysNaissanceLibelle='" + paysNaissanceLibelle + '\'' +
                ", adresse=" + adresse +
                '}';
    }
}
