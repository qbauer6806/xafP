package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidNatureLienEnum;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidConjointDTO implements Serializable {

    private static final long serialVersionUID = -2115988260505091076L;

    private ResidCiviliteEnum conjointCivilite;

    private String conjointNom;

    private String conjointPrenom;

    private String conjointDateNaissance;

    private String conjointNationaliteCode;

    private ResidNatureLienEnum conjointRelation;

    private boolean conjointFoyer;

    public ResidCiviliteEnum getConjointCivilite() {
        return conjointCivilite;
    }

    public void setConjointCivilite(ResidCiviliteEnum conjointCivilite) {
        this.conjointCivilite = conjointCivilite;
    }

    public String getConjointNom() {
        return conjointNom;
    }

    public void setConjointNom(String conjointNom) {
        this.conjointNom = conjointNom;
    }

    public String getConjointPrenom() {
        return conjointPrenom;
    }

    public void setConjointPrenom(String conjointPrenom) {
        this.conjointPrenom = conjointPrenom;
    }

    public String getConjointDateNaissance() {
        return conjointDateNaissance;
    }

    public void setConjointDateNaissance(String conjointDateNaissance) {
        this.conjointDateNaissance = conjointDateNaissance;
    }

    public String getConjointNationaliteCode() {
        return conjointNationaliteCode;
    }

    public void setConjointNationaliteCode(String conjointNationaliteCode) {
        this.conjointNationaliteCode = conjointNationaliteCode;
    }

    public ResidNatureLienEnum getConjointRelation() {
        return conjointRelation;
    }

    public void setConjointRelation(ResidNatureLienEnum conjointRelation) {
        this.conjointRelation = conjointRelation;
    }

    public boolean isConjointFoyer() {
        return conjointFoyer;
    }

    public void setConjointFoyer(boolean conjointFoyer) {
        this.conjointFoyer = conjointFoyer;
    }
}
