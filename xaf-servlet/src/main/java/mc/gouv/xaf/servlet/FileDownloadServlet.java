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
 * Servlet servant à télécharger un fichier de FILE.
 * 
 * @author qdeme
 *
 */
public class FileDownloadServlet extends FileServlet {

    private static final long serialVersionUID = -2464829773835748491L;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /filedownload doGet()");

        try {
            super.doGet(request, response, false);
        } catch (Exception e) {
            LOGGER.error("FileDownloadServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /filedownload doGet()");

    }

}
