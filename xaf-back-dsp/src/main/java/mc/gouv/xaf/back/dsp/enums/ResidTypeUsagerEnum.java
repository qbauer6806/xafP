package mc.gouv.xaf.back.dsp.enums;

public enum ResidTypeUsagerEnum {

    UsagerNouveau("UsagerNouveau"),
    UsagerExistant ("UsagerExistant");

    public String value;

    ResidTypeUsagerEnum(String value) {
        this.value = value;
    }
}
