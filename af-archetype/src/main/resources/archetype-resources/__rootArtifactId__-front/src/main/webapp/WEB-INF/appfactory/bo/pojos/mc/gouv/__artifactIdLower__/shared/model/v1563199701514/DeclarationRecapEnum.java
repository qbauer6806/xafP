#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeclarationRecapEnum {

    TEXTE("Je sollicite l’attribution de l’aide de l’Etat pour l’achat du véhicule propre décrit à l’étape 3, sous réserve qu’il satisfasse aux conditions édictées par l'Arrêté Ministériel n°2018-1182 du 18/12/18.${symbol_escape}nJe m’engage à rembourser cette aide, au prorata temporis, en cas de vente avant 3 ans pour un quatre-roues et 2 ans pour un vélo, deux-roues, tricycle ou quadricycle à moteur.", "TEXTE", "texte");


    public String libelle;
    public String originalName;
    public String camelName;

    private DeclarationRecapEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        DeclarationRecapEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static DeclarationRecapEnum forValue(String value) {
	return forValue(value, false);
    }

    public static DeclarationRecapEnum forValue(String value, boolean choixMultiple) {
        for (DeclarationRecapEnum checkEnum : values()) {
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
