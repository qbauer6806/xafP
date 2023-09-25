package mc.gouv.xaf.servlet.util;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Cette classe permet de créer un objet de requête java avec une méthode GET qui peut contenir un coprs/body
 * Cette classe est nécessaire car dans de nombreuses libraires, une exception et jetée lorsqu'on ajout un coprs à une requête en GET.
 */
public class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {
    @Override
    public String getMethod() {
        return "GET";
    }
}