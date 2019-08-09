#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VehiculeEmission2Enum {

    MOINS_DE_20("<= à 20", "MOINS_DE_20", "moinsDe_20"),
    DE_21_A_50("De 21 à 50", "DE_21_A_50", "de_21A_50");


    public String libelle;
    public String originalName;
    public String camelName;

    private VehiculeEmission2Enum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        VehiculeEmission2Enum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static VehiculeEmission2Enum forValue(String value) {
	return forValue(value, false);
    }

    public static VehiculeEmission2Enum forValue(String value, boolean choixMultiple) {
        for (VehiculeEmission2Enum checkEnum : values()) {
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
