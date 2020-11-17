package mc.gouv.xaf.shared.itg.resid.enums;

public enum ResidIdentificationTypeEnum {

    NumeroResident("NumeroResident"),
    NumeroCarte  ("NumeroCarte  ");

    public String value;

    ResidIdentificationTypeEnum(String value) {
        this.value = value;
    }
}
