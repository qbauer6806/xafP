#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VehiculeTypeEnum {

    QUATRE_ROUES("Quatre-roues", "QUATRE_ROUES", "quatreRoues"),
    DEUX_ROUES("Deux-roues", "DEUX_ROUES", "deuxRoues"),
    QUADRICYCLE("Quadricycle", "QUADRICYCLE", "quadricycle"),
    TAXI_ET_GRANDE_REMISE("Taxi ou Grande Remise", "TAXI_ET_GRANDE_REMISE", "taxiEtGrandeRemise");


    public String libelle;
    public String originalName;
    public String camelName;

    private VehiculeTypeEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        VehiculeTypeEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static VehiculeTypeEnum forValue(String value) {
	return forValue(value, false);
    }

    public static VehiculeTypeEnum forValue(String value, boolean choixMultiple) {
        for (VehiculeTypeEnum checkEnum : values()) {
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
