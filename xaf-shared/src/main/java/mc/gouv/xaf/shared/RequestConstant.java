package mc.gouv.xaf.shared;

public class RequestConstant {

    public static final String REDIRECT = "redirect:";

    public static final String LANGUE_PARAM = "langue";
    public static final String DEMANDES_ID_PARAM = "demandesId";
    public static final String USAGERID_PARAM = "usagerId";
    public static final String CLIENT_ID_PARAM = "client_id";
    public static final String CLIENT_SECRET_PARAM = "client_secret";
    public static final String SCOPE_PARAM = "scope";
    public static final String REFRESH_TOKEN_PARAM = "refresh_token";
    public static final String PAGE_PARAM = "page";
    public static final String SIZE_PARAM = "size";
    public static final String SORT_PARAM = "sort";
    public static final String DIRECTION_PARAM = "direction";
    public static final String STATUS_PARAM = "status";
    public static final String LANG_PARAM = "lang";

    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";

    public static final String DEMANDES_PATH = "demandes";
    public static final String CONFIGS_PATH = "configs";
    public static final String ACCESSES_PATH = "accesses";
    public static final String BROUILLONS_PATH = "brouillons";
    public static final String COMPLEMENTS_PATH = "complements";
    public static final String TIMESTAMP_MODIFICATION = "timestamp";
    public static final String ID_PARAM = "id";
    public static final String TYPE_PATH = "type";

    private RequestConstant() {
        throw new IllegalStateException("Utility class");
    }
}
