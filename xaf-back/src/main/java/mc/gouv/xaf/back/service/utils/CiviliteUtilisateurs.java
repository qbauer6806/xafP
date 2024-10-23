package mc.gouv.xaf.back.service.utils;

public enum CiviliteUtilisateurs {
    MONSIEUR(1, "M.", "Monsieur"),
    MADAME(2, "Mme.", "Madame"),
    MONSEIGNEUR(3, "Mgr.", "Monseigneur");

    private Integer code;
    private String libelle;
    private String abbreviation;

    CiviliteUtilisateurs(Integer code, String abbreviation, String libelle) {
        this.code = code;
        this.libelle = libelle;
        this.abbreviation = abbreviation;
    }

    public String getLibelle() {
        return libelle;
    }

    public Integer getCode() {
        return code;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

}
