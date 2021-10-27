package mc.gouv.xaf.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.enums.HttpMethod;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.dto.BrouillonDTO;

/**
 * Servlet mettant à disposition le service /brouillons avec les méthodes PUT, POST, GET, DELETE.
 * Cette servlet récupère le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS
 * correspondants dans le back-end générique.
 * 
 * @author qdeme
 *
 */
public class BrouillonsServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static Logger LOGGER = LoggerFactory.getLogger(BrouillonsServlet.class);

    public HttpServletResponse doHttpMethod(HttpServletRequest request, HttpServletResponse response,
            HttpMethod httpMethod) throws UnsupportedOperationException, IOException {

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
        }

        String pathInfo = request.getPathInfo();
        String brouillonId = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            String[] pathElems = pathInfo.split("/");
            brouillonId = pathElems[1];
        }

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();

        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);

        LOGGER.info("DemarcheID=" + demarcheId + ", UsagerID=" + usagerId + ", BrouillonID=" + brouillonId);

        AfApiClient afApiClient = getAfApiClient();
        ObjectMapper mapper = new ObjectMapper();

        String repJson = null;
        if (HttpMethod.PUT.equals(httpMethod) || HttpMethod.POST.equals(httpMethod)) {
            // Récupération du JSON reçu en input et transmission au 2ème service en
            // UTF8
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.toString().length() == 0) {
                return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                        "Erreur: JSON manquant");
            }

            LOGGER.info("Appel à la démarche pour créer le brouillon");
            BrouillonDTO brouillonInput = mapper.readValue(buffer.toString(), BrouillonDTO.class);
            BrouillonDTO brouillonDto = null;
            if (HttpMethod.POST.equals(httpMethod)) {
            	brouillonDto = afApiClient.creerBrouillon(brouillonInput, usagerId);
            	response.setStatus(HttpStatus.SC_CREATED);
            }
            else {
            	brouillonDto = afApiClient.updateBrouillon(brouillonInput, Integer.parseInt(brouillonId));
            	response.setStatus(HttpStatus.SC_OK);
            }

            // TODO : gestion des erreurs
            repJson = mapper.writeValueAsString(brouillonDto);
            IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());

        } else if (HttpMethod.GET.equals(httpMethod)) {
        	if (StringUtils.isBlank(brouillonId)) {
	            LOGGER.info("Appel à la démarche pour récupérer tous les brouillons de l'usager");
	            List<BrouillonDTO> brouillonDtos = afApiClient.getBrouillons(usagerId);
	            // TODO : gestion des erreurs
	            response.setStatus(HttpStatus.SC_OK);
	            repJson = mapper.writeValueAsString(brouillonDtos);
        	}
        	else {
	            LOGGER.info("Appel à la démarche pour récupérer le brouillon " + brouillonId);
	            BrouillonDTO brouillonDto = afApiClient.getBrouillon(Integer.parseInt(brouillonId));
	            // TODO : gestion des erreurs
	            response.setStatus(HttpStatus.SC_OK);
	            repJson = mapper.writeValueAsString(brouillonDto);
        	}
        	IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
        } else if (HttpMethod.DELETE.equals(httpMethod)) {
        	LOGGER.info("Appel à la démarche pour supprimer le brouillon");
        	afApiClient.deleteBrouillon(Integer.parseInt(brouillonId));
        }

        response.setContentType("application/json");

        return response;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /brouillons doPost()");

        try {
            doHttpMethod(request, response, HttpMethod.POST);
        } catch (IOException e) {
            LOGGER.error("BrouillonsServlet - Une erreur est survenue lors de l'appel à la méthode POST", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /brouillons doPost()");
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /brouillons doPut()");

        try {
            doHttpMethod(request, response, HttpMethod.PUT);
        } catch (IOException e) {
            LOGGER.error("BrouillonsServlet - Une erreur est survenue lors de l'appel à la méthode PUT", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /brouillons doPut()");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /brouillons doGet()");

        try {
            doHttpMethod(request, response, HttpMethod.GET);
        } catch (Exception e) {
            LOGGER.error("BrouillonsServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /brouillons doGet()");
    }
    
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /brouillons doDelete()");

        try {
            doHttpMethod(request, response, HttpMethod.DELETE);
        } catch (Exception e) {
            LOGGER.error("BrouillonsServlet - Une erreur est survenue lors de l'appel à la méthode DELETE", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /brouillons doDelete()");
    }
}
