package mc.gouv.xaf.back.dsp.enums.common;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ResidTypeUsagerEnum {

    USAGER_NOUVEAU("UsagerNouveau"),
    USAGER_EXISTANT("UsagerExistant");

    private String value;

    ResidTypeUsagerEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
