#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VehiculeTypetousEnum {

    CAT1("Quatre-roues", "CAT1", "cat1"),
    CAT2("Deux-roues, tricycle, quadricycle, vélo électrique avec moteur >250W", "CAT2", "cat2"),
    CAT3("Vélo électrique avec moteur <=250W", "CAT3", "cat3");


    public String libelle;
    public String originalName;
    public String camelName;

    private VehiculeTypetousEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        VehiculeTypetousEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static VehiculeTypetousEnum forValue(String value) {
	return forValue(value, false);
    }

    public static VehiculeTypetousEnum forValue(String value, boolean choixMultiple) {
        for (VehiculeTypetousEnum checkEnum : values()) {
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
