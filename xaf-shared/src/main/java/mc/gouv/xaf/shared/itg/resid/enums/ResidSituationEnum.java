package mc.gouv.xaf.shared.itg.resid.enums;

public enum ResidSituationEnum {

    SALARIE_A_SON_COMPTE("SALARIE_A_SON_COMPTE"),
    ETUDIANT("ETUDIANT"),
    CHOMEUR("CHOMEUR"),
    RETRAITE("RETRAITE"),
    AU_FOYER("AU_FOYER"),
    AUTRE("AUTRE");

    public String value;

    ResidSituationEnum(String value) {
        this.value = value;
    }
}
