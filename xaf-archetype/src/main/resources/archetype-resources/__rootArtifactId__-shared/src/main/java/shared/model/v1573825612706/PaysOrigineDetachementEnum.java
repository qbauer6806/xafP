#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1573825612706;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaysOrigineDetachementEnum {

    FR("France", "FR", "fr"),
    IT("Italie", "IT", "it");


    public String libelle;
    public String originalName;
    public String camelName;

    private PaysOrigineDetachementEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        PaysOrigineDetachementEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static PaysOrigineDetachementEnum forValue(String value) {
	return forValue(value, false);
    }

    public static PaysOrigineDetachementEnum forValue(String value, boolean choixMultiple) {
        for (PaysOrigineDetachementEnum checkEnum : values()) {
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
