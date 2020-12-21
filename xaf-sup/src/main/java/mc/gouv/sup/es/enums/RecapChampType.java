package mc.gouv.sup.es.enums;

public enum RecapChampType {

    CHAINE("chaine"),
    CHOIX("choix"),
    DATE("date"),
    ADRESSE("adresse"),
    ADRESSE_MC("adresseMc"),
    IBAN("iban");

    private String type;

    private RecapChampType(String type) {
        this.type = type;
    }

    public static RecapChampType getFromType(String type) {
        if (type != null) {
            for (RecapChampType recapChampType : values()) {
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