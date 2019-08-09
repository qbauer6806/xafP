#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeclarationTexteEnum {

    TEXTE("Je sollicite l’attribution de l’aide de l’Etat pour l’achat du véhicule propre décrit à l’étape 3, sous réserve qu’il satisfasse aux conditions édictées par <a href=${symbol_escape}"https://journaldemonaco.gouv.mc/Journaux/2018/Journal-8413/Arrete-Ministeriel-n-2018-1182-du-18-decembre-2018-relatif-a-l-aide-a-l-achat-de-vehicules-ecologiques${symbol_escape}" title=${symbol_escape}"Arrêté Ministériel n° 2018-1182 du 18 décembre 2018 relatif à laide à lachat de véhicules écologiques${symbol_escape}" class=${symbol_escape}"newWindow${symbol_escape}">${symbol_escape}nArrêté Ministériel n°2018-1182 du 18/12/18</a>. <br><em></em>${symbol_escape}nJe m’engage à rembourser cette aide, au prorata temporis, en cas de vente avant 3 ans pour un quatre-roues et 2 ans pour un vélo, deux-roues, tricycle ou quadricycle à moteur.", "TEXTE", "texte");


    public String libelle;
    public String originalName;
    public String camelName;

    private DeclarationTexteEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        DeclarationTexteEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static DeclarationTexteEnum forValue(String value) {
	return forValue(value, false);
    }

    public static DeclarationTexteEnum forValue(String value, boolean choixMultiple) {
        for (DeclarationTexteEnum checkEnum : values()) {
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
