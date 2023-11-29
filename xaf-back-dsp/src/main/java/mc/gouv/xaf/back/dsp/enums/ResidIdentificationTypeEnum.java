package mc.gouv.xaf.back.dsp.enums;

public enum ResidIdentificationTypeEnum {

    NumeroResident("NumeroResident"),
    NumeroCarte  ("NumeroCarte");

    String value;

    ResidIdentificationTypeEnum(String value) {
        this.value = value;
    }
}
