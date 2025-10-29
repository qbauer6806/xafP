package mc.gouv.xaf.shared.enums;

public enum RecapChampTypeEnum {
    CHAINE("chaine"),
    CHOIX("choix"),
    DATE("date"),
    ADRESSE("adresse"),
    ADRESSE_MC("adresseMc"),
    TABLEAU("tableau"),
    TELEPHONE("telephone"),
    IBAN("iban");

    private String type;

    private RecapChampTypeEnum(String type) {
        this.type = type;
    }

    public static RecapChampTypeEnum getFromType(String type) {
        if (type != null) {
            for (RecapChampTypeEnum recapChampType : values()) {
                if (type.equals(recapChampType.getType())) {
                    return recapChampType;
                }
            }
        }
        return CHAINE;
    }

    public String getType() {
        return type;
    }

}
