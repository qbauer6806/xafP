package mc.gouv.xaf.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;

/**
 * 
 * Proxy vers le référentiel Pays
 * 
 * @author qdeme
 *
 */
public class PaysServlet extends AbstractAfServlet {

    private static final long serialVersionUID = 4105537492545284465L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaysServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /pays doGet()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }
        String pathToQuery = request.getPathInfo();
        String queryString = request.getQueryString();

        String serviceUrl = AfServletGouvPropertiesResolver.getPaysUrl() + (pathToQuery != null ? pathToQuery : "")
                + (queryString != null ? "?" + queryString : "");

        LOGGER.info("Appel à {}", serviceUrl);

        Request serviceRequest = Request.Get(serviceUrl);
        serviceRequest.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            response.setStatus(statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                response.setContentType(serviceResponse.getEntity().getContentType().getValue());
                IOUtils.copy(serviceResponse.getEntity().getContent(), response.getOutputStream());
            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors du traitement de la réponse", e);
        }

        LOGGER.info("====================== Fin /pays doGet()");
    }

}
