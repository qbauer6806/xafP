package mc.gouv.xaf.servlet.util;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Cette classe permet de créer un objet de requête java avec une méthode qui ne pourrait pas contenir un coprs/body autrement.
 * Par exemple, il est normalement impossible d'envoyer un body dans une requête GET.
 * Cette classe permet de contourner cette limitation
 */
public class HttpRequestWithEntity extends HttpEntityEnclosingRequestBase {
    private final String method;

    public HttpRequestWithEntity(String method) {
        this.method = method;
    }

    @Override
    public String getMethod() {
        return method;
    }
}