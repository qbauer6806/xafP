package mc.gouv.xaf.shared.itg.resid.enums;

public enum ResidTypeUsagerEnum {
    // TODO Resid Supprimer UsagerExistantAllOf
    UsagerNouveau("UsagerNouveau"),
    UsagerExistant ("UsagerExistant"),
    UsagerExistantAllOf ("UsagerExistantAllOf");

    public String value;

    ResidTypeUsagerEnum(String value) {
        this.value = value;
    }
}
