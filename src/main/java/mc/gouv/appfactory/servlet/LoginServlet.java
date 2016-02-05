package mc.gouv.appfactory.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mc.gouv.Static;

/**
 * 
 * @author qdeme
 *
 */
public class LoginServlet extends HttpServlet {
    
    private static final String LOGIN_REST_URL = Static.getValue("appfactoryLoginRestUrl", "http://linuxas-dev/login/rest") + "/loggedUsagers/";
    
    private static final long serialVersionUID = -394488730959377371L;
    
    private static Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        
        // Le SessionID est stocké dans l'URL parameter "id"
        String sessionId = request.getParameter("id");
        
        LOGGER.info("/login doPut() sessionId=" + sessionId);
        
        String serviceUrl = LOGIN_REST_URL + sessionId; 
        Request serviceRequest = Request.Get(serviceUrl);
        serviceRequest.setHeader("Accept", "application/json");
        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int code = serviceResponse.getStatusLine().getStatusCode();
            if (code == HttpServletResponse.SC_NOT_FOUND || code != HttpServletResponse.SC_OK) {
                response.setStatus(HttpStatus.SC_NOT_FOUND);
            }
            else {
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").create();
                UsagerInfos uinfos = gson.fromJson(new InputStreamReader(serviceResponse.getEntity().getContent(), Charset.forName("UTF-8")), UsagerInfos.class);
                
                if (uinfos != null) {
                    // Stockage de cet objet d'infos d'usager dans la session HTTP
                    HttpSession session = request.getSession();
                    session.setAttribute("login", uinfos);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        
        LOGGER.info("Fin /login doPut()");
            
    }
    
}
