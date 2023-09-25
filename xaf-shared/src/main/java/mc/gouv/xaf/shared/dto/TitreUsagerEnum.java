package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TitreUsagerEnum {

    _0("Monsieur", "0"),
    _1("Madame", "1"),
    _2("Mademoiselle", "2");


    public String libelle;
    public String value;

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
