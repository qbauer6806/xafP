package mc.gouv.appfactory.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.appfactory.dto.UsagerInfosDTO;

/**
 * 
 * @author qdeme
 *
 */
public class SessionsServlet extends HttpServlet {

    private static final long serialVersionUID = -7833206552171322810L;
    
    private static Logger LOGGER = LoggerFactory.getLogger(SessionsServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        
        LOGGER.info("/sessions doGet()");
        
        // On tente de récupérer une session existante sans en créer une
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // Récupération de l'objet attaché à la session
            UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO)session.getAttribute("login");
            
            // Retour au client
            response.setContentType("application/json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
            mapper.writeValue(response.getOutputStream(), usagerInfosDTO);
            response.getOutputStream().flush();
        }
        else {
            // Pas de session trouvée
            response.setStatus(HttpStatus.SC_NOT_FOUND);
        }
        
        LOGGER.info("Fin /sessions doGet()");
    }

}
