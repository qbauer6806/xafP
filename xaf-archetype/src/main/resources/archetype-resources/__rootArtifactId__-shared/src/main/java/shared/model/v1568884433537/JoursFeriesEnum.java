#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1568884433537;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JoursFeriesEnum {

    JOUR_DE_L_AN("Le jour de l'An", "JOUR_DE_L_AN", "jourDeL_An"),
    SAINTE_DEVOTE("La Sainte Dévote", "SAINTE_DEVOTE", "sainteDevote"),
    LUNDI_DE_PAQUES("Le lundi de Pâques", "LUNDI_DE_PAQUES", "lundiDePaques"),
    LE1ER_MAI("Le 1er mai", "LE1ER_MAI", "le1erMai"),
    ASCENSION("L'Ascension", "ASCENSION", "ascension"),
    LUNDI_DE_PENTECOTE("Le lundi de Pentecôte", "LUNDI_DE_PENTECOTE", "lundiDePentecote"),
    FETE_DIEU("La Fête Dieu", "FETE_DIEU", "feteDieu"),
    ASSOMPTION("L'Assomption", "ASSOMPTION", "assomption"),
    TOUSSAINT("La Toussaint", "TOUSSAINT", "toussaint"),
    FETE_DU_PRINCE("La Fête du Prince", "FETE_DU_PRINCE", "feteDuPrince"),
    IMMACULEE_CONCEPTION("L'Immaculée Conception", "IMMACULEE_CONCEPTION", "immaculeeConception"),
    NOEL("La Noël", "NOEL", "noel");


    public String libelle;
    public String originalName;
    public String camelName;

    private JoursFeriesEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        JoursFeriesEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static JoursFeriesEnum forValue(String value) {
	return forValue(value, false);
    }

    public static JoursFeriesEnum forValue(String value, boolean choixMultiple) {
        for (JoursFeriesEnum checkEnum : values()) {
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
