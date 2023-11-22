package mc.gouv.xaf.back.dsp.enums;

public enum ResidTypeUsagerEnum {

    UsagerNouveau("UsagerNouveau"),
    UsagerExistant ("UsagerExistant");

    String value;

    ResidTypeUsagerEnum(String value) {
        this.value = value;
    }
}
