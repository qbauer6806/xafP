package mc.gouv.xaf.back.dsp.enums.common;

public enum ResidLoyerPeriodiciteEnum {

    JOURNALIER("JOURNALIER"),
    MENSUEL("MENSUEL"),
    TRIMESTRIEL("TRIMESTRIEL"),
    SEMESTRIEL("SEMESTRIEL"),
    ANNUEL("ANNUEL"),
    AUCUN("AUCUN");

    public String value;

    ResidLoyerPeriodiciteEnum(String value) {
        this.value = value;
    }
}
