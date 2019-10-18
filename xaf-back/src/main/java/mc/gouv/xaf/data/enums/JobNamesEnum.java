package mc.gouv.xaf.data.enums;

public enum JobNamesEnum {

    REINDEXATION("Réindexation des demandes");

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
