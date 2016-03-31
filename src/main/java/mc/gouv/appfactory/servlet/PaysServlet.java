package mc.gouv.appfactory.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.appfactory.util.AppFactoryServletUtils;

/**
 * Proxy vers le référentiel Pays
 * 
 * @author qdeme
 *
 */
public class PaysServlet extends HttpServlet {

    private static final long serialVersionUID = 4105537492545284465L;
    
    private static Logger LOGGER = LoggerFactory.getLogger(PaysServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /pays doGet()");
        
        String pathToQuery = request.getPathInfo();
        String queryString = request.getQueryString();
        
        String serviceUrl = AppFactoryServletUtils.PAYS_URL + (pathToQuery != null ? pathToQuery : "") + (queryString != null ? "?" + queryString : "");
        
        LOGGER.info("Appel à " + serviceUrl);
        
        Request serviceRequest = Request.Get(serviceUrl);
        serviceRequest.setHeader("Accept", "application/json");
        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            response.setStatus(statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                response.setContentType(serviceResponse.getEntity().getContentType().getValue());
                IOUtils.copy(serviceResponse.getEntity().getContent(), response.getOutputStream());
            }
        }
        catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors du traitement de la réponse",e);
        }
        
        LOGGER.info("====================== Fin /pays doGet()");
    }
    
}
