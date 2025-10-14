package mc.gouv.xaf.back.service.itg.logon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum Etat {
    @JsonProperty("a") ACTIF('a', "Actif"),
    @JsonProperty("i") INACTIF('i', "Inactif");

    private final char code;
    private final String libelle;

    Etat(char code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }
    
}
