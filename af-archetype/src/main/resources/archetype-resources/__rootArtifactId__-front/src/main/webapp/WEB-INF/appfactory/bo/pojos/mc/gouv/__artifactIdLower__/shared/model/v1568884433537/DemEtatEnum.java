#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1568884433537;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DemEtatEnum {

    EN_ATTENTE_TRAIT("En attente de traitement", "EN_ATTENTE_TRAIT", "enAttenteTrait"),
    EN_COURS_TRAIT("En cours de traitement", "EN_COURS_TRAIT", "enCoursTrait"),
    ACCEPTEE("Acceptée", "ACCEPTEE", "acceptee"),
    ACCEPTEE_SOUS_RESERVE("Acceptée sous réserve", "ACCEPTEE_SOUS_RESERVE", "accepteeSousReserve"),
    REFUSEE("Refusée", "REFUSEE", "refusee"),
    RECEVABLE("Recevable", "RECEVABLE", "recevable"),
    EN_ATTENTE_COMPL("En attente d’informations complémentaires", "EN_ATTENTE_COMPL", "enAttenteCompl"),
    VALIDEE("Validée", "VALIDEE", "validee"),
    ANNULEE("Annulée", "ANNULEE", "annulee"),
    EN_ATTENTE_FINALISATION("En attente de finalisation", "EN_ATTENTE_FINALISATION", "enAttenteFinalisation"),
    ACCORDEE("Accordée", "ACCORDEE", "accordee");


    public String libelle;
    public String originalName;
    public String camelName;

    private DemEtatEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        DemEtatEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static DemEtatEnum forValue(String value) {
	return forValue(value, false);
    }

    public static DemEtatEnum forValue(String value, boolean choixMultiple) {
        for (DemEtatEnum checkEnum : values()) {
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
