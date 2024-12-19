package mc.gouv.xaf.back.dsp.enums.common;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ResidIdentificationTypeEnum {

    NUMERO_RESIDENT("NumeroResident"),
    NUMERO_CARTE("NumeroCarte");

    private String value;

    ResidIdentificationTypeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
