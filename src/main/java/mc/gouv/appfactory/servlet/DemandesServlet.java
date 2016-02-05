package mc.gouv.appfactory.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author qdeme
 *
 */
public class DemandesServlet extends HttpServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static Logger LOGGER = LoggerFactory.getLogger(DemandesServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("POST");
    }
}
