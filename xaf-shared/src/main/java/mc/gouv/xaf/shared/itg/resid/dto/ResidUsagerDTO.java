package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.shared.itg.resid.enums.ResidTypeUsagerEnum;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidUsagerDTO implements Serializable {

    private static final long serialVersionUID = 759419710680261692L;

    private ResidTypeUsagerEnum usagerType;

    private ResidResidentDTO identification;

    private ResidIdentiteNouvelleDTO identite;

    private ResidNationaliteDTO nationalitePrincipale;

    private ResidNationaliteDTO nationaliteAutre;

    private ResidAdresseDTO adresseMonaco;

    private ResidAdresseDTO adresseProvenance;

    private ResidLogementDTO logement;

    private ResidHebergeantDTO hebergeant;

    private ResidMoyensExistenceDTO moyensExistence;

    private ResidConjointDTO conjoint;

    public ResidTypeUsagerEnum getUsagerType() {
        return usagerType;
    }

    public void setUsagerType(ResidTypeUsagerEnum usagerType) {
        this.usagerType = usagerType;
    }

    public ResidResidentDTO getIdentification() {
        return identification;
    }

    public void setIdentification(ResidResidentDTO identification) {
        this.identification = identification;
    }

    public ResidIdentiteNouvelleDTO getIdentite() {
        return identite;
    }

    public void setIdentite(ResidIdentiteNouvelleDTO identite) {
        this.identite = identite;
    }

    public ResidNationaliteDTO getNationalitePrincipale() {
        return nationalitePrincipale;
    }

    public void setNationalitePrincipale(ResidNationaliteDTO nationalitePrincipale) {
        this.nationalitePrincipale = nationalitePrincipale;
    }

    public ResidNationaliteDTO getNationaliteAutre() {
        return nationaliteAutre;
    }

    public void setNationaliteAutre(ResidNationaliteDTO nationaliteAutre) {
        this.nationaliteAutre = nationaliteAutre;
    }

    public ResidAdresseDTO getAdresseMonaco() {
        return adresseMonaco;
    }

    public void setAdresseMonaco(ResidAdresseDTO adresseMonaco) {
        this.adresseMonaco = adresseMonaco;
    }

    public ResidAdresseDTO getAdresseProvenance() {
        return adresseProvenance;
    }

    public void setAdresseProvenance(ResidAdresseDTO adresseProvenance) {
        this.adresseProvenance = adresseProvenance;
    }

    public ResidLogementDTO getLogement() {
        return logement;
    }

    public void setLogement(ResidLogementDTO logement) {
        this.logement = logement;
    }

    public ResidHebergeantDTO getHebergeant() {
        return hebergeant;
    }

    public void setHebergeant(ResidHebergeantDTO hebergeant) {
        this.hebergeant = hebergeant;
    }

    public ResidMoyensExistenceDTO getMoyensExistence() {
        return moyensExistence;
    }

    public void setMoyensExistence(ResidMoyensExistenceDTO moyensExistence) {
        this.moyensExistence = moyensExistence;
    }

    public ResidConjointDTO getConjoint() {
        return conjoint;
    }

    public void setConjoint(ResidConjointDTO conjoint) {
        this.conjoint = conjoint;
    }
}
