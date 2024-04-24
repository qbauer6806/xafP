package mc.gouv.xaf.back.dsp.enums.common;

public enum ResidMotifChangementSituationEnum {

    ADR("ADR"),
    ETA("ETA"),
    NAT("NAT"),
    NOU("NOU"),
    PRO("PRO"),
    REC("REC"),
    PAS("PAS");

    public String value;

    ResidMotifChangementSituationEnum(String value) {
        this.value = value;
    }

}
