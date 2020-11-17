package mc.gouv.xaf.shared.itg.resid.enums;

public enum ResidCarteTypeEnum {

    PRIMO_ARRIVANT_FRANCE("PRIMO_ARRIVANT_FRANCE"),
    PRIMO_ARRIVANT_EEE("PRIMO_ARRIVANT_EEE"),
    PRIMO_ARRIVANT_HORS_EEE("PRIMO_ARRIVANT_HORS_EEE"),
    PRIMO_ARRIVANT_HORS_EEE_PAR_TRANSFERT("PRIMO_ARRIVANT_HORS_EEE_PAR_TRANSFERT"),
    PREMIERE_DEMANDE_MINEUR_16_ANS("PREMIERE_DEMANDE_MINEUR_16_ANS");

    public String value;

    ResidCarteTypeEnum(String value) {
        this.value = value;
    }
}
