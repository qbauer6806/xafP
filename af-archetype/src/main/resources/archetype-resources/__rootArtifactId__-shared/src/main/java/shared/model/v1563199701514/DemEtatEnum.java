#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DemEtatEnum {

    EN_ATTENTE_TRAIT("En attente de traitement", "EN_ATTENTE_TRAIT", "enAttenteTrait"),
    EN_COURS_TRAIT("En cours de traitement", "EN_COURS_TRAIT", "enCoursTrait"),
    REFUSEE("Refusée", "REFUSEE", "refusee"),
    EN_ATTENTE_COMPL("En attente d’informations complémentaires", "EN_ATTENTE_COMPL", "enAttenteCompl"),
    ANNULEE("Annulée", "ANNULEE", "annulee"),
    VALIDEE_EN_ATTENTE_PAIEMENT("Validée et en attente de paiement", "VALIDEE_EN_ATTENTE_PAIEMENT", "valideeEnAttentePaiement"),
    VALIDEE_ET_PAYEE("Validée et payée", "VALIDEE_ET_PAYEE", "valideeEtPayee");


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
