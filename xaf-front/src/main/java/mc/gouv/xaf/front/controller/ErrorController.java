package mc.gouv.candifp.frontserver.movetoxaf.controller;

import com.google.gson.*;
import mc.gouv.candifp.frontserver.movetoxaf.dto.UsagerInfosDTO;
import mc.gouv.xaf.shared.SharedMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 
 * Servlet permettant de faire apparaître dans les logs du frontserver, les erreurs survenant dans la console
 * du navigateur de l'usager.
 * 
 * @author qdeme
 *
 */

@Controller
@RequestMapping("/logerror")
public class ErrorController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorController.class);

	@Autowired
	private XafFrontserverUtils xafFrontserverUtils;

    @PostMapping
    public ResponseEntity doPost(HttpServletRequest request) {
        LOGGER.info("====================== /error doPost()");
        
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
			return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
					SharedMessages.UTILISATEUR_NON_AUTORISE);
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
				return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
	                    "Erreur: JSON manquant");
	        }
	        
	        String jsonError = buffer.toString();
	        LOGGER.error("Erreur reçue du FO (json brut) : {}", jsonError);
	        
	        JsonArray array = JsonParser.parseString(jsonError).getAsJsonArray();

	        // Affichage de la stacktrace de chacune des erreurs
	        if (array != null) {
		        for (JsonElement elem : array) {
		        	JsonObject obj = elem.getAsJsonObject();
		        	JsonElement stack = obj.get("stack");
		        	if (stack != null && !(stack instanceof JsonNull)) {
		        		LOGGER.error(stack.getAsString());
		        	}
		        }
	        }
        
		} catch (IOException | JsonSyntaxException e) {
			LOGGER.error("Erreur dans ErrorServlet", e);
		}
        
        LOGGER.info("====================== Fin /error doPost()");

		return ResponseEntity.ok().build();
    }

}