package mc.gouv.xaf.shared.itg.resid.enums;

public enum ResidTypeUsagerEnum {

    UsagerNouveau("UsagerNouveau"),
    UsagerExistant ("UsagerExistant ");

    public String value;

    ResidTypeUsagerEnum(String value) {
        this.value = value;
    }
}
