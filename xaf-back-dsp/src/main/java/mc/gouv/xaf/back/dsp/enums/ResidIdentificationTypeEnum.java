package mc.gouv.xaf.back.dsp.enums;

public enum ResidIdentificationTypeEnum {

    NUMERO_RESIDENT("NumeroResident"),
    NUMERO_CARTE("NumeroCarte");

    String value;

    ResidIdentificationTypeEnum(String value) {
        this.value = value;
    }
}
