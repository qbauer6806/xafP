package mc.gouv.xaf.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DocHolderFilesServlet extends AbstractAfServlet {
    private final static long serialVersionUID = -314577095316396789L;
    private static Logger LOGGER = LoggerFactory.getLogger(DocHolderFilesServlet.class);

    /**
     * Méthode pour l'opération <b>getMultipleFiles</b>
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new UnsupportedOperationException("Méthode non implémentée");
    }

    /**
     * Méthode pour l'opération <b>deleteMultipleFiles</b>
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new UnsupportedOperationException("Méthode non implémentée");
    }
}
