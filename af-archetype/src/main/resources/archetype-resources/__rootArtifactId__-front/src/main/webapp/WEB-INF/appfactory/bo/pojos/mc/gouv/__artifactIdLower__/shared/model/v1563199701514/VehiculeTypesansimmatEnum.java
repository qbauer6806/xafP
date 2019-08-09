#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VehiculeTypesansimmatEnum {

    TRICYLE("Tricycle", "TRICYLE", "tricyle"),
    VELO_ELECTRIQUE_AVEC_MOTEUR_SUPERIEUR_A_250_W("Vélo éléctrique avec moteur supérieur à 250 watt", "VELO_ELECTRIQUE_AVEC_MOTEUR_SUPERIEUR_A_250_W", "veloElectriqueAvecMoteurSuperieurA_250W"),
    VELO_ELECTRIQUE_AVEC_MOTEUR_INFERIEUR_A_250_WATT("Vélo éléctrique avec moteur inférieur ou égal à 250 watt", "VELO_ELECTRIQUE_AVEC_MOTEUR_INFERIEUR_A_250_WATT", "veloElectriqueAvecMoteurInferieurA_250Watt");


    public String libelle;
    public String originalName;
    public String camelName;

    private VehiculeTypesansimmatEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        VehiculeTypesansimmatEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static VehiculeTypesansimmatEnum forValue(String value) {
	return forValue(value, false);
    }

    public static VehiculeTypesansimmatEnum forValue(String value, boolean choixMultiple) {
        for (VehiculeTypesansimmatEnum checkEnum : values()) {
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
