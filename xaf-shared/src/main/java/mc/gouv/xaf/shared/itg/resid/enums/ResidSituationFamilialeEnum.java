package mc.gouv.xaf.shared.itg.resid.enums;

public enum ResidSituationFamilialeEnum {

    CON("CON"),
    CEL("CEL"),
    DIV("DIV"),
    MAR("MAR"),
    SEP("SEP"),
    VEU("VEU"),
    PAR("PAR"),
    COH("COH"),

    // A utiliser pour le conjoint
    EPO("EPO");

    public String value;

    ResidSituationFamilialeEnum(String value) {
        this.value = value;
    }
}
