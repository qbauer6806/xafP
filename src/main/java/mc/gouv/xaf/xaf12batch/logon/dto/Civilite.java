package mc.gouv.xaf.xaf12batch.logon.dto;

import java.util.Arrays;
import java.util.List;

public enum Civilite {
    MONSIEUR(1, "Monsieur"),
    MADAME(2, "Madame"),
    MONSEIGNEUR(3, "Monseigneur");

    private Integer code;
    private String libelle;

    private Civilite(Integer code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    public String getLibelle() {
        return this.libelle;
    }

    public Integer getCode() {
        return this.code;
    }

    public static Civilite getCiviliteFromCode(Integer code) {
        Civilite[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Civilite civilite = var1[var3];
            if (civilite.code.equals(code)) {
                return civilite;
            }
        }

        return null;
    }

    public static String getCiviliteLibelleFromCode(Integer code) {
        Civilite[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Civilite civilite = var1[var3];
            if (civilite.code.equals(code)) {
                return civilite.libelle;
            }
        }

        return null;
    }

    public static List<Civilite> getAllCivilites() {
        return Arrays.asList(values());
    }
}
