#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1568884433537;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TitreEnum {

    _0("Monsieur", "0", "0"),
    _1("Madame", "1", "1"),
    _2("Mademoiselle", "2", "2");


    public String libelle;
    public String originalName;
    public String camelName;

    private TitreEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        TitreEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static TitreEnum forValue(String value) {
	return forValue(value, false);
    }

    public static TitreEnum forValue(String value, boolean choixMultiple) {
        for (TitreEnum checkEnum : values()) {
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
