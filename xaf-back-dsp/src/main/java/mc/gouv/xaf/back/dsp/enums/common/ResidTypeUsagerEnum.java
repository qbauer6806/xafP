package mc.gouv.xaf.back.dsp.enums.common;

public enum ResidTypeUsagerEnum {

    USAGER_NOUVEAU("UsagerNouveau"),
    USAGER_EXISTANT("UsagerExistant");

    String value;

    ResidTypeUsagerEnum(String value) {
        this.value = value;
    }
}
