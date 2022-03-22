package mc.gouv.xaf.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mc.gouv.xaf.servlet.enums.HttpMethod;
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
 * 
 * Servlet mettant à disposition le service /customRequest avec les méthodes PUT, POST, GET, DELETE.
 * Cette servlet permet d'appeler des fonctions API custom/spécifiques d'une démarche
 * 
 * @author qdeme
 *
 */
public class CustomRequestServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static Logger LOGGER = LoggerFactory.getLogger(CustomRequestServlet.class);

    public HttpServletResponse doHttpMethod(HttpServletRequest request, HttpServletResponse response,
            HttpMethod httpMethod) throws IOException {

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
        if (serviceRequest == null) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Situation anormale : serviceRequest == null");
            return response;
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
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /customRequest doPost()");

        try {
            doHttpMethod(request, response, HttpMethod.POST);
        } catch (Exception e) {
            LOGGER.error("CustomRequestServlet - Une erreur est survenue lors de l'appel à la méthode POST", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /customRequest doPost()");
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /customRequest doPut()");

        try {
            doHttpMethod(request, response, HttpMethod.PUT);
        } catch (Exception e) {
            LOGGER.error("CustomRequestServlet - Une erreur est survenue lors de l'appel à la méthode PUT", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /customRequest doPut()");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /customRequest doGet()");

        try {
            doHttpMethod(request, response, HttpMethod.GET);
        } catch (Exception e) {
            LOGGER.error("CustomRequestServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /customRequest doGet()");
    }
    
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandes doDelete()");

        try {
            doHttpMethod(request, response, HttpMethod.DELETE);
        } catch (Exception e) {
            LOGGER.error("CustomRequestServlet - Une erreur est survenue lors de l'appel à la méthode DELETE", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /demandes doDelete()");
    }
}
