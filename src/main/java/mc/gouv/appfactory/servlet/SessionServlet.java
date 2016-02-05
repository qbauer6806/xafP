package mc.gouv.appfactory.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author qdeme
 *
 */
public class SessionServlet extends HttpServlet {

    private static final long serialVersionUID = -7833206552171322810L;
    
    private static Logger LOGGER = LoggerFactory.getLogger(SessionServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        
        LOGGER.info("/session doGet()");
        
        // On tente de récupérer une session existante sans en créer une
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // Récupération de l'objet attaché à la session
            UsagerInfos usagerInfos = (UsagerInfos)session.getAttribute("login");
            
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").create();
            
            // Retour au client
            response.getOutputStream().print(gson.toJson(usagerInfos));
            response.getOutputStream().flush();
        }
        else {
            // Pas de session trouvée
            response.setStatus(HttpStatus.SC_NOT_FOUND);
        }
        
        LOGGER.info("Fin /session doGet()");
    }

}
