package mc.gouv.xaf.shared.itg.resid.enums;

public enum ResidCanalCommunicationEnum {

    SMS("SMS"),
    EMA("EMA"),
    EMS("EMS"),
    AUC("AUC");

    public String value;

    ResidCanalCommunicationEnum(String value) {
        this.value = value;
    }
}
