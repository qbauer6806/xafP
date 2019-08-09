#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VehiculeEmissionEnum {

    MOINS_DE_20("<= à 20", "MOINS_DE_20", "moinsDe_20"),
    DE_21_A_50("De 21 à 50", "DE_21_A_50", "de_21A_50"),
    DE_51_A_60("De 51 à 60", "DE_51_A_60", "de_51A_60"),
    DE_61_A_110("De 61 à 110", "DE_61_A_110", "de_61A_110");


    public String libelle;
    public String originalName;
    public String camelName;

    private VehiculeEmissionEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        VehiculeEmissionEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static VehiculeEmissionEnum forValue(String value) {
	return forValue(value, false);
    }

    public static VehiculeEmissionEnum forValue(String value, boolean choixMultiple) {
        for (VehiculeEmissionEnum checkEnum : values()) {
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
