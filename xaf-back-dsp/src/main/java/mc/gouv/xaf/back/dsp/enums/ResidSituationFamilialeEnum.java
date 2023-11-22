package mc.gouv.xaf.back.dsp.enums;

public enum ResidSituationFamilialeEnum {

    CON("CON"),
    CEL("CEL"),
    DIV("DIV"),
    MAR("MAR"),
    SEP("SEP"),
    VEU("VEU"),
    PAR("PAR"),
    COH("COH");

    String value;

    ResidSituationFamilialeEnum(String value) {
        this.value = value;
    }
}
