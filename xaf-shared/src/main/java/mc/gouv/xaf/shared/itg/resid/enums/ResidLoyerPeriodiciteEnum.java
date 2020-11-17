package mc.gouv.xaf.shared.itg.resid.enums;

public enum ResidLoyerPeriodiciteEnum {

    JOURNALIER("JOURNALIER"),
    MENSUEL("MENSUEL"),
    TRIMESTRIEL("TRIMESTRIEL"),
    SEMESTRIEL("SEMESTRIEL"),
    ANNUEL("ANNUEL"),
    AUCUNE("AUCUNE");

    public String value;

    ResidLoyerPeriodiciteEnum(String value) {
        this.value = value;
    }
}
