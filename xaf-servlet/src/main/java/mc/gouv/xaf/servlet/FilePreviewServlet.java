package mc.gouv.xaf.servlet;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * Servlet servant à visualiser dans le navigateur un fichier de FILE.
 * 
 * @author uek
 *
 */
public class FilePreviewServlet extends FileServlet {

    private static final long serialVersionUID = -2464829773835748491L;

    private static final Logger LOGGER = LoggerFactory.getLogger(FilePreviewServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /filepreview doGet()");

        try {
            super.doGet(request, response, true);
        } catch (Exception e) {
            LOGGER.error("FilePreviewServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /filepreview doGet()");

    }

}
