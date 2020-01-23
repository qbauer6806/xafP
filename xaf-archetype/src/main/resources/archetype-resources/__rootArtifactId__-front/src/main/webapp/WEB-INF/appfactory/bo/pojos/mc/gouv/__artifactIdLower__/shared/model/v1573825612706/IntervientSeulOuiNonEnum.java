#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1573825612706;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IntervientSeulOuiNonEnum {

    OUI("Oui", "OUI", "oui"),
    NON("Non, j’interviens seul", "NON", "non");


    public String libelle;
    public String originalName;
    public String camelName;

    private IntervientSeulOuiNonEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        IntervientSeulOuiNonEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static IntervientSeulOuiNonEnum forValue(String value) {
	return forValue(value, false);
    }

    public static IntervientSeulOuiNonEnum forValue(String value, boolean choixMultiple) {
        for (IntervientSeulOuiNonEnum checkEnum : values()) {
            if (checkEnum.name().equalsIgnoreCase(value) || checkEnum.originalName.equalsIgnoreCase(value)) {
                return checkEnum;
            }
	    if (choixMultiple && checkEnum.camelName.equalsIgnoreCase(value)) {
                return checkEnum;
	    }
        }
        return null;
    }

    @JsonValue
    public String toValue() {
        return this.originalName;
    }

    @Override
    public String toString() {
        return this.libelle;
    }
}
