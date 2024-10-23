package mc.gouv.xaf.shared.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TitreUsagerEnum {

    TITRE_0("Monsieur", "0"),
    TITRE_1("Madame", "1"),
    TITRE_2("Mademoiselle", "2");

    String libelle;
    String value;

    TitreUsagerEnum(String libelle, String value) {
        this.libelle = libelle;
        this.value = value;
    }

    @JsonValue
    public String getLibelle() {
        return libelle;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}
