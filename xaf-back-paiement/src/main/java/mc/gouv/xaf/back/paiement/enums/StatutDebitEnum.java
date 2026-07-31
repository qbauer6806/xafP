package mc.gouv.xaf.back.paiement.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatutDebitEnum {

    PAID("PAID"),
    UNPAID("UNPAID"),
    PENDING("PENDING");

    private final String libelle;

    StatutDebitEnum(String libelle) {
        this.libelle = libelle;
    }

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static StatutDebitEnum fromValue(String value) {
        for (StatutDebitEnum e : values()) {
            if (e.name().equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
