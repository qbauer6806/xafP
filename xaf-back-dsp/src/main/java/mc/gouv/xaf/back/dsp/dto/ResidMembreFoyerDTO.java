package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidMembreRelationEnum;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidMembreFoyerDTO implements Serializable {

    private static final long serialVersionUID = 5659961517651205985L;

    private ResidCiviliteEnum membreCivilite;

    private String membreNom;

    private String membrePrenom;

    private String membreDateNaissance;

    private String membreNationaliteCode;

    private ResidMembreRelationEnum membreRelation;

    private boolean membreFoyer;

    public ResidCiviliteEnum getMembreCivilite() {
        return membreCivilite;
    }

    public void setMembreCivilite(ResidCiviliteEnum membreCivilite) {
        this.membreCivilite = membreCivilite;
    }

    public String getMembreNom() {
        return membreNom;
    }

    public void setMembreNom(String membreNom) {
        this.membreNom = membreNom;
    }

    public String getMembrePrenom() {
        return membrePrenom;
    }

    public void setMembrePrenom(String membrePrenom) {
        this.membrePrenom = membrePrenom;
    }

    public String getMembreDateNaissance() {
        return membreDateNaissance;
    }

    public void setMembreDateNaissance(String membreDateNaissance) {
        this.membreDateNaissance = membreDateNaissance;
    }

    public String getMembreNationaliteCode() {
        return membreNationaliteCode;
    }

    public void setMembreNationaliteCode(String membreNationaliteCode) {
        this.membreNationaliteCode = membreNationaliteCode;
    }

    public ResidMembreRelationEnum getMembreRelation() {
        return membreRelation;
    }

    public void setMembreRelation(ResidMembreRelationEnum membreRelation) {
        this.membreRelation = membreRelation;
    }

    public boolean isMembreFoyer() {
        return membreFoyer;
    }

    public void setMembreFoyer(boolean membreFoyer) {
        this.membreFoyer = membreFoyer;
    }
}
