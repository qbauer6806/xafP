package mc.gouv.xaf.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;

/**
 * Servlet mettant à disposition le service /customRequest avec les méthodes PUT, POST, GET, DELETE.
 * Cette servlet permet d'appeler des fonctions API custom/spécifiques d'une démarche
 * 
 * @author qdeme
 *
 */
public class CustomRequestServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static Logger LOGGER = LoggerFactory.getLogger(CustomRequestServlet.class);

    private enum HttpMethod {
        PUT,
        POST,
        GET,
        DELETE;
    }

    public HttpServletResponse doHttpMethod(HttpServletRequest request, HttpServletResponse response,
            HttpMethod httpMethod) throws UnsupportedOperationException, IOException {

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
        }

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();

        LOGGER.info("UsagerID=" + usagerId);

        String pathInfo = request.getPathInfo();
        String restOfUrl = null;
        if (pathInfo != null && pathInfo.length() > 1) {
        	restOfUrl = "/" + pathInfo.split("/")[1];
        }
        
        String serviceUrl = AfServletGouvPropertiesResolver.getApiUrl() + "/customRequest";
        
        if (StringUtils.isNotBlank(restOfUrl)) {
        	serviceUrl += restOfUrl;
        }
        
        if (StringUtils.isNotBlank(request.getQueryString())) {
        	serviceUrl += "?" + request.getQueryString();
        }

        LOGGER.info("Appel à " + serviceUrl);

        Request serviceRequest = null;
        if (HttpMethod.GET.equals(httpMethod)) {
        	serviceRequest = Request.Get(serviceUrl);
        }
        else if (HttpMethod.POST.equals(httpMethod)) {
        	serviceRequest = Request.Post(serviceUrl);
        	serviceRequest.bodyByteArray(IOUtils.toString(request.getInputStream()).getBytes());
        }
        else if (HttpMethod.PUT.equals(httpMethod)) {
        	serviceRequest = Request.Put(serviceUrl);
        	serviceRequest.bodyByteArray(IOUtils.toString(request.getInputStream()).getBytes());
        }
        else if (HttpMethod.DELETE.equals(httpMethod)) {
        	serviceRequest = Request.Delete(serviceUrl);
        }
        serviceRequest.setHeader("Authorization", "Bearer " + AfServletGouvPropertiesResolver.getApiJwt());
        
        // Copier les headers
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
        	String elem = headers.nextElement();
        	if (!"Content-Length".equals(elem)) {
        		serviceRequest.setHeader(elem, request.getHeader(elem));
        	}
        }
        
        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            response.setStatus(statusCode);
            response.setContentType(serviceResponse.getEntity().getContentType().getValue());
            IOUtils.copy(serviceResponse.getEntity().getContent(), response.getOutputStream());
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors du traitement de la réponse", e);
        }

        return response;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /customRequest doPost()");

        response = doHttpMethod(request, response, HttpMethod.POST);

        LOGGER.info("====================== Fin /customRequest doPost()");
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /customRequest doPut()");

        response = doHttpMethod(request, response, HttpMethod.PUT);

        LOGGER.info("====================== Fin /customRequest doPut()");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /customRequest doGet()");

        response = doHttpMethod(request, response, HttpMethod.GET);

        LOGGER.info("====================== Fin /customRequest doGet()");
    }
    
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /demandes doDelete()");

        response = doHttpMethod(request, response, HttpMethod.DELETE);

        LOGGER.info("====================== Fin /demandes doDelete()");
    }
}
