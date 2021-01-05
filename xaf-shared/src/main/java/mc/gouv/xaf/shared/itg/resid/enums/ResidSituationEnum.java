package mc.gouv.xaf.shared.itg.resid.enums;

public enum ResidSituationEnum {

    SALARIE_A_SON_COMPTE("SALARIE_A_SON_COMPTE"),
    ETUDIANT("ETUDIANT"),
    CHOMEUR("CHOMEUR"),
    RETRAITE("RETRAITE"),
    AU_FOYER("AU_FOYER"),
    SANS_PROFESSION("SANS_PROFESSION"),
    INVALIDITE_HANDICAP_MALADIE("INVALIDITE_HANDICAP_MALADIE"),
    RENTIER("RENTIER");

    public String value;

    ResidSituationEnum(String value) {
        this.value = value;
    }
}
