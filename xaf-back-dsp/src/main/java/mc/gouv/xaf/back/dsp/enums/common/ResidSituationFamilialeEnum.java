package mc.gouv.xaf.back.dsp.enums.common;

public enum ResidSituationFamilialeEnum {

    CON("CON"),
    CEL("CEL"),
    DIV("DIV"),
    MAR("MAR"),
    SEP("SEP"),
    VEU("VEU"),
    PAR("PAR"),
    COH("COH");

    public String value;

    ResidSituationFamilialeEnum(String value) {
        this.value = value;
    }
}
