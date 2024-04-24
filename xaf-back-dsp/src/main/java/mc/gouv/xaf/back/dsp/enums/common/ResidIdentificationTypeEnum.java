package mc.gouv.xaf.back.dsp.enums.common;

public enum ResidIdentificationTypeEnum {

    NumeroResident("NumeroResident"),
    NumeroCarte  ("NumeroCarte");

    public String value;

    ResidIdentificationTypeEnum(String value) {
        this.value = value;
    }
}
