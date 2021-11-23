package mc.gouv.xaf.servlet;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;

/**
 * Servlet permettant de faire apparaître dans les logs du frontserver, les erreurs survenant dans la console
 * du navigateur de l'usager.
 * 
 * @author qdeme
 *
 */
public class ErrorServlet extends AbstractAfServlet {

    private static final long serialVersionUID = 520893456441444275L;

    private static Logger LOGGER = LoggerFactory.getLogger(ErrorServlet.class);

 
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /error doPost()");
        
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }

        // Récupération du JSON reçu en input
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader;
		try {
			reader = request.getReader();

	        String line;
	        while ((line = reader.readLine()) != null) {
	            buffer.append(line);
	        }
	
	        if (buffer.toString().length() == 0) {
	            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
	                    "Erreur: JSON manquant");
	        }
	        
	        String jsonError = buffer.toString();
	        LOGGER.error("Erreur reçue du FO (json brut) : " + jsonError);
	        
	        JsonArray array = JsonParser.parseString(jsonError).getAsJsonArray();

	        // Affichage de la stacktrace de chacune des erreurs
	        if (array != null) {
		        for (JsonElement elem : array) {
		        	JsonObject obj = elem.getAsJsonObject();
		        	JsonElement stack = obj.get("stack");
		        	if (stack != null) {
		        		LOGGER.error(stack.getAsString());
		        	}
		        }
	        }
        
		} catch (IOException e) {
			LOGGER.error("Erreur dans ErrorServlet", e);
		}
        
        LOGGER.info("====================== Fin /error doPost()");
    }

}