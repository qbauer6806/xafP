package mc.gouv.xaf.back.dsp.enums.common;

public enum ResidMembreRelationEnum {

    PRO("PRO"),
    AMI("AMI"),
    BEA("BEA"),
    PAR("PAR"),
    GRA("GRA"),
    COU("COU"),
    FRA("FRA"),
    NEV("NEV"),
    GEN("GEN"),
    ENF("ENF"),
    PET("PET"),
    BFI("BFI"),
    BFR("BFR"),
    EPO("EPO"),
    XPO("XPO"),
    CON("CON"),
    CDV("CDV");

    String value;

    ResidMembreRelationEnum(String value) {
        this.value = value;
    }
}
