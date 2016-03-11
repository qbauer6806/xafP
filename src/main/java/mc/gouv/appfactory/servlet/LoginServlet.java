package mc.gouv.appfactory.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.appfactory.dto.UsagerInfosDTO;
import mc.gouv.appfactory.util.AppFactoryServletUtils;

/**
 * 
 * @author qdeme
 *
 */
public class LoginServlet extends HttpServlet {
    
    private static final long serialVersionUID = -394488730959377371L;
    
    private static Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        
        // Le SessionID est stocké dans l'URL parameter "id"
        String sessionId = request.getParameter("id");
        
        LOGGER.info("/login doPost() sessionId=" + sessionId);
        
        if (StringUtils.isBlank(sessionId)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Appel du service ts-login
        String serviceUrl = AppFactoryServletUtils.LOGIN_REST_URL + "/" + sessionId;
        Request serviceRequest = Request.Get(serviceUrl);
        serviceRequest.setHeader("Accept", "application/json");
        try {
            LOGGER.info("Appel du service ts-login...");
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int code = serviceResponse.getStatusLine().getStatusCode();
            if (code == HttpServletResponse.SC_NOT_FOUND || code != HttpServletResponse.SC_OK) {
                response.setStatus(HttpStatus.SC_NOT_FOUND);
            }
            else {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
                UsagerInfosDTO uinfos = mapper.readValue(serviceResponse.getEntity().getContent(), UsagerInfosDTO.class);

                if (uinfos != null) {
                    // Stockage de cet objet d'infos d'usager dans la session HTTP
                    HttpSession session = request.getSession();
                    session.setAttribute("login", uinfos);
                }
            }
        }
        catch (Exception e) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Erreur interne: " + e.toString() + e.getMessage());
        }
        
        LOGGER.info("Fin /login doPost()");
            
    }
    
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        
        // Le SessionID est stocké dans l'URL parameter "id"
        String sessionId = request.getParameter("id");
        
        LOGGER.info("/login doDelete() sessionId=" + sessionId);
        
        if (StringUtils.isBlank(sessionId)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Appel du service ts-login
        String serviceUrl = AppFactoryServletUtils.LOGIN_REST_URL + "/" + sessionId;
        Request serviceRequest = Request.Delete(serviceUrl);
        try {
            LOGGER.info("Appel du service ts-login...");
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            response.setStatus(statusCode);
            // Si tout s'est bien passé, alors on détruit la session côté AppFactoryServlet
            if (statusCode == HttpServletResponse.SC_NO_CONTENT) {
                request.getSession().invalidate();
            }
        }
        catch (Exception e) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Erreur interne: " + e.toString() + e.getMessage());
        }
        
        LOGGER.info("Fin /login doDelete()");
            
    }
    
}
