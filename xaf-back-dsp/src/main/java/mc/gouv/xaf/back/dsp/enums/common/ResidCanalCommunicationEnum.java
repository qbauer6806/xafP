package mc.gouv.xaf.back.dsp.enums.common;

public enum ResidCanalCommunicationEnum {

    SMS("SMS"),
    EMA("EMA"),
    EMS("EMS"),
    AUC("AUC"),
    PAP("PAP");

    String value;

    ResidCanalCommunicationEnum(String value) {
        this.value = value;
    }
}
