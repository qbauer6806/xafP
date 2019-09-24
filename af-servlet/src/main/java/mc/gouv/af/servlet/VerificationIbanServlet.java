package mc.gouv.af.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.af.servlet.dto.TgfApiIbanResponseDTO;
import mc.gouv.af.servlet.dto.TgfApiIbanResponseErreurDTO;
import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.af.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.af.servlet.util.AppFactoryServletUtils;

/**
 * Servlet permettant l'appel à la méthode /verification-iban de l'API TGF
 * 
 * @author qdeme
 *
 */
public class VerificationIbanServlet extends AbstractAfServlet {

    private static final long serialVersionUID = 520893456441444275L;

    private static Logger LOGGER = LoggerFactory.getLogger(VerificationIbanServlet.class);

 
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /verification-iban doPost()");
        
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }

        Request serviceRequest = Request.Post(AfServletGouvPropertiesResolver.getTgfApiUrl());
        serviceRequest.setHeader("Content-Type", "application/json; charset=utf-8");
        serviceRequest.setHeader("Authorization", "Bearer " + AfServletGouvPropertiesResolver.getTgfApiJwt());
        serviceRequest.bodyStream(request.getInputStream());
        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            response.setStatus(statusCode);
            response.setContentType(serviceResponse.getEntity().getContentType().getValue());
            
            String responseContent = IOUtils.toString(serviceResponse.getEntity().getContent());
            LOGGER.info("Response content:" + responseContent);
            if (StringUtils.isBlank(responseContent)) {
            	IOUtils.copy(serviceResponse.getEntity().getContent(), response.getOutputStream());
            }
            else {
	            ObjectMapper mapper = new ObjectMapper();
	            TgfApiIbanResponseDTO responsePojo = mapper.readValue(responseContent, TgfApiIbanResponseDTO.class);
	            List<TgfApiIbanResponseErreurDTO> newErreurList = new ArrayList<TgfApiIbanResponseErreurDTO>();
	            for (TgfApiIbanResponseErreurDTO erreur : responsePojo.getErreurs()) {
	            	if (!"mc.gouv.tgf.api.iban.iban.codePaysIbanBicNonCorrespondant".equals(erreur.getCode())) {
	            		newErreurList.add(erreur);
	            	}
	            }
	            
	            // Si après suppression de l'erreur codePaysIbanBicNonCorrespondant, il ne reste plus d'erreurs, alors remettre un statut 200
	            if (newErreurList.size() == 0) {
	            	LOGGER.info("Final response OK");
	            	response.setStatus(HttpStatus.SC_OK);
	            	IOUtils.write("", response.getOutputStream());
	            }
	            else {
	            	LOGGER.info("Final response NOK, with error list");
		            responsePojo.setErreurs(newErreurList.toArray(new TgfApiIbanResponseErreurDTO[newErreurList.size()]));
		            String newResponse = mapper.writeValueAsString(responsePojo);
		            
		            IOUtils.copy(new ByteArrayInputStream(newResponse.getBytes()), response.getOutputStream());	
	            }
            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("Erreur lors de l'appel à l'API TGF", e);
        }

        LOGGER.info("====================== Fin /verification-iban doPost()");
    }

}