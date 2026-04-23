package mc.gouv.xaf.back.service.js;

public final class DynamicJSBuilderUtils {

    public static final String DEBUT = "\n(\"";
    public static final String FIN = "\";\n}\n";
    public static final String RETURN = "return \"";
    public static final String RETURN_INCONNU = "return \"INCONNU\";\n}\n";

    private DynamicJSBuilderUtils() {
    }

    public static boolean ifElse(StringBuilder builder, boolean first) {
        if (first) {
            builder.append("\nif ");
        } else {
            builder.append("\nelse if ");
        }
        return false;
    }
}
