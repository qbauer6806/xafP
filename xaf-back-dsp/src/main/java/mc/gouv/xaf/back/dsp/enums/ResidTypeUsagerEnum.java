package mc.gouv.xaf.back.dsp.enums;

public enum ResidTypeUsagerEnum {

    USAGER_NOUVEAU("UsagerNouveau"),
    UsagerExistant ("UsagerExistant");

    String value;

    ResidTypeUsagerEnum(String value) {
        this.value = value;
    }
}
