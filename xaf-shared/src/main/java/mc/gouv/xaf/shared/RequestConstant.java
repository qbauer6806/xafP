package mc.gouv.xaf.shared;

public class RequestConstant {
    public static final String LANGUE_PARAM = "langue";
    public static final String DEMANDES_ID_PARAM = "demandesId";
    public static final String USAGERID_PARAM = "usagerId";

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String DEMANDES_PATH = "demandes";
    public static final String ACCESSES_PATH = "accesses";
    public static final String BROUILLONS_PATH = "brouillons";

    private RequestConstant() {
        throw new IllegalStateException("Utility class");
    }
}
