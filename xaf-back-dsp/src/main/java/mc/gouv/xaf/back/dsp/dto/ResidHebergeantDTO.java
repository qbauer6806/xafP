package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidQualiteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidSexeEnum;
import mc.gouv.xaf.back.dsp.enums.v2.ResidRelationEnum;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidHebergeantDTO implements Serializable {

    private static final long serialVersionUID = -2474143139113858294L;

    private ResidCiviliteEnum hebergeantCivilite;

    private String hebergeantNomRaisonSociale;

    @JsonInclude()
    private String hebergeantNomUsage;

    @JsonInclude()
    private String hebergeantPrenom;

    private ResidSexeEnum hebergeantSexe;

    @JsonInclude()
    private String hebergantNationaliteCode;

    private ResidQualiteEnum hebegeantQualite;

    private ResidRelationEnum hebergeantRelation;

    public String getHebergeantNomRaisonSociale() {
        return hebergeantNomRaisonSociale;
    }

    public void setHebergeantNomRaisonSociale(String hebergeantNomRaisonSociale) {
        this.hebergeantNomRaisonSociale = hebergeantNomRaisonSociale;
    }

    public ResidCiviliteEnum getHebergeantCivilite() {
        return hebergeantCivilite;
    }

    public void setHebergeantCivilite(ResidCiviliteEnum hebergeantCivilite) {
        this.hebergeantCivilite = hebergeantCivilite;
    }

    public String getHebergeantNomUsage() {
        return hebergeantNomUsage;
    }

    public void setHebergeantNomUsage(String hebergeantNomUsage) {
        this.hebergeantNomUsage = hebergeantNomUsage;
    }

    public String getHebergeantPrenom() {
        return hebergeantPrenom;
    }

    public void setHebergeantPrenom(String hebergeantPrenom) {
        this.hebergeantPrenom = hebergeantPrenom;
    }

    public ResidSexeEnum getHebergeantSexe() {
        return hebergeantSexe;
    }

    public void setHebergeantSexe(ResidSexeEnum hebergeantSexe) {
        this.hebergeantSexe = hebergeantSexe;
    }

    public String getHebergantNationaliteCode() {
        return hebergantNationaliteCode;
    }

    public void setHebergantNationaliteCode(String hebergantNationaliteCode) {
        this.hebergantNationaliteCode = hebergantNationaliteCode;
    }

    public ResidQualiteEnum getHebegeantQualite() {
        return hebegeantQualite;
    }

    public void setHebegeantQualite(ResidQualiteEnum hebegeantQualite) {
        this.hebegeantQualite = hebegeantQualite;
    }

    public ResidRelationEnum getHebergeantRelation() {
        return hebergeantRelation;
    }

    public void setHebergeantRelation(ResidRelationEnum hebergeantRelation) {
        this.hebergeantRelation = hebergeantRelation;
    }
}
