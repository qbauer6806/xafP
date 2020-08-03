package mc.gouv.xaf.shared.dto;

public enum JobNamesEnum {

    GET_DEMANDES_DESYNCHRONISEES("Afficher demandes désynchronisées"),
    REINDEXATION_DEMANDES_DESYNCHRO("Réindexation des demandes désynchronisées"),
    REINDEXATION("Réindexation Globale"),
    REINDEXATION_DEMANDES("Réindexation des demandes");

    private String libelle;

    private JobNamesEnum(String libelle) {
        this.libelle = libelle;
    }

    public static JobNamesEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (JobNamesEnum jobNameEnum : values()) {
            if (jobNameEnum.name().equals(name)) {
                return jobNameEnum;
            }
        }

        return null;

    }

    public String getLibelle() {
        return libelle;
    }

}
