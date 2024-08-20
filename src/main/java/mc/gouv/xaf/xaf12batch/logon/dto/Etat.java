package mc.gouv.xaf.xaf12batch.logon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public enum Etat {
    @JsonProperty("a")
    ACTIF('a', "Actif"),
    @JsonProperty("i")
    INACTIF('i', "Inactif");

    private char code;
    private String libelle;

    Etat(char code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    public char getCode() {
        return this.code;
    }

    public String getLibelle() {
        return this.libelle;
    }

    public String getCodeAsString() {
        return this.code + "";
    }

    public static Etat getFromCodeString(String code) {
        return StringUtils.isEmpty(code) ? null : getFromCode(code.charAt(0));
    }

    public static Etat getFromCode(char code) {
        return switch (code) {
            case 'a' -> ACTIF;
            case 'i' -> INACTIF;
            default -> null;
        };
    }
}
