package mc.gouv.xaf.back.service.itg.logon.dto;

import lombok.Getter;

@Getter
public enum Civilite {
    MONSIEUR(1, "Monsieur"),
    MADAME(2, "Madame"),
    MONSEIGNEUR(3, "Monseigneur");

    private final Integer code;
    private final String libelle;

    Civilite(Integer code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

}
