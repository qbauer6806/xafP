package mc.gouv.sup.es.enums;

public enum EsType {

    TEXT("text"),
    CHOIX_MULTIPLE("choixMultiple"),
    KEYWORD("keyword"),
    DATE("date");

    private String type;

    public static EsType getFromType(String type) {
        if (type != null) {
            for (EsType estype : values()) {
                if (type.equals(estype.getType())) {
                    return estype;
                }
            }
        }
        return null;
    }

    private EsType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static String getEsType(String type) {

        if (EsType.getFromType(type) != null) {
            return type;
        }
        switch (RecapChampType.getFromType(type)) {
            case CHAINE:
            case CHOIX:
            case ADRESSE:
            case ADRESSE_MC:
            case IBAN:
                return EsType.TEXT.getType();
            case DATE:
                return EsType.DATE.getType();
            default:
                return null;

        }
    }

}
