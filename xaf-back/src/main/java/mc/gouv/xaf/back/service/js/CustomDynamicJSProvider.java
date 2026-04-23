package mc.gouv.xaf.back.service.js;

@FunctionalInterface
public interface CustomDynamicJSProvider {

    void appendCustomJs(StringBuilder builder);
}
