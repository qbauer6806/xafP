#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SimulationMontantEnum {

    MONTANT_DE_3000_EUROS("3000 euros", "MONTANT_DE_3000_EUROS", "montantDe_3000Euros"),
    MONTANT_DE_6000_EUROS("6000 euros", "MONTANT_DE_6000_EUROS", "montantDe_6000Euros"),
    MONTANT_DE_5000_EUROS("5000 euros", "MONTANT_DE_5000_EUROS", "montantDe_5000Euros"),
    MONTANT_CALCULE_DE_30_DU_PRIX_TTC_MAX_400_EUROS("30 % du prix TTC - max 400 euros", "MONTANT_CALCULE_DE_30_DU_PRIX_TTC_MAX_400_EUROS", "montantCalculeDe_30DuPrixTtcMax_400Euros"),
    MONTANT_CALCULE_DE_30_DU_PRIX_TTC_MAX_3000_EUROS("30 % du prix TTC - max 3000 euros", "MONTANT_CALCULE_DE_30_DU_PRIX_TTC_MAX_3000_EUROS", "montantCalculeDe_30DuPrixTtcMax_3000Euros"),
    MONTANT_CALCULE_DE_30_DU_PRIX_TTC_MAX_1000_EUROS("30 % du prix TTC - max 10000 euros", "MONTANT_CALCULE_DE_30_DU_PRIX_TTC_MAX_1000_EUROS", "montantCalculeDe_30DuPrixTtcMax_1000Euros"),
    MONTANT_DE_800_EUROS("800 euros", "MONTANT_DE_800_EUROS", "montantDe_800Euros"),
    MONTANT_DE_PLUS_DE_3000("+3000 euros", "MONTANT_DE_PLUS_DE_3000", "montantDePlusDe_3000"),
    UNINIT("0", "UNINIT", "uninit");


    public String libelle;
    public String originalName;
    public String camelName;

    private SimulationMontantEnum(String libelle, String originalName, String camelName) {
        this.libelle = libelle;
        this.originalName = originalName;
	this.camelName = camelName;
    }

    public static final String getLibelle(String code) {
        SimulationMontantEnum val = forValue(code);
        if (val != null) {
            return val.libelle;
        }
        return code;
    }

    @JsonCreator
    public static SimulationMontantEnum forValue(String value) {
	return forValue(value, false);
    }

    public static SimulationMontantEnum forValue(String value, boolean choixMultiple) {
        for (SimulationMontantEnum checkEnum : values()) {
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
