package mc.gouv.xaf.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DocHolderServlet extends AbstractAfServlet {
    private final static long serialVersionUID = -314577095316396789L;
    private static Logger LOGGER = LoggerFactory.getLogger(DocHolderServlet.class);

    /**
     * Methode pour l'opération <b>createDocumentHolder</b>
     * Elle permet la création d'un nouveau "document-holder" ou "porte-document"
     *
     * @param req
     * @param resp
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        throw new UnsupportedOperationException("Méthode non implémentée");
    }

    /**
     * Méthode pour l'opération <b>deleteDocumentHolder</b>
     * Elle permet la destruction d'un porte-document.
     *
     * @param req
     * @param resp
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        throw new UnsupportedOperationException("Méthode non implémentée");
    }
}
