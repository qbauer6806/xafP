package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import mc.gouv.xaf.back.dsp.enums.ResidIdentificationTypeEnum;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidResidentDTO implements Serializable {

    private static final long serialVersionUID = -2065671673609703252L;

    private ResidIdentificationTypeEnum identificationType;

    private String numeroResident;

    private String numeroCarte;

    public ResidIdentificationTypeEnum getIdentificationType() {
        return identificationType;
    }

    public void setIdentificationType(ResidIdentificationTypeEnum identificationType) {
        this.identificationType = identificationType;
    }

    public String getNumeroResident() {
        return numeroResident;
    }

    public void setNumeroResident(String numeroResident) {
        this.numeroResident = numeroResident;
    }

    public String getNumeroCarte() {
        return numeroCarte;
    }

    public void setNumeroCarte(String numeroCarte) {
        this.numeroCarte = numeroCarte;
    }
}
