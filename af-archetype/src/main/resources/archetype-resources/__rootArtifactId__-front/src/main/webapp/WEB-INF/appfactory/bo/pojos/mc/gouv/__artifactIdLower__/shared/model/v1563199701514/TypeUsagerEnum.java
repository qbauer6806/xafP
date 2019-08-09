#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TypeUsagerEnum {

    PARTICULIER("Un véhicule particulier", "PARTICULIER", "particulier"),
    ENTREPRISE("Un véhicule d’entreprise", "ENTREPRISE", "entreprise");


    public String libelle;
    public String originalName;
    public String camelName;

    private TypeUsagerEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        TypeUsagerEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static TypeUsagerEnum forValue(String value) {
	return forValue(value, false);
    }

    public static TypeUsagerEnum forValue(String value, boolean choixMultiple) {
        for (TypeUsagerEnum checkEnum : values()) {
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
