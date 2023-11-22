package mc.gouv.xaf.back.dsp.enums;

public enum ResidCanalCommunicationEnum {

    SMS("SMS"),
    EMA("EMA"),
    EMS("EMS"),
    AUC("AUC");

    String value;

    ResidCanalCommunicationEnum(String value) {
        this.value = value;
    }
}
