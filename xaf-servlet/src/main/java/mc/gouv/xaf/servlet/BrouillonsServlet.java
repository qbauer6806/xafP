package mc.gouv.xaf.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import mc.gouv.xaf.shared.SharedMessages;
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
 * 
 * Servlet mettant à disposition le service /brouillons avec les méthodes PUT, POST, GET, DELETE.
 * Cette servlet récupère le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS
 * correspondants dans le back-end générique.
 * 
 * @author qdeme
 *
 */
public class BrouillonsServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static final Logger LOGGER = LoggerFactory.getLogger(BrouillonsServlet.class);

    /**
     * Prépare les objets communs aux requêtes :<br>
     * <ol>
     *     <li>Un UsagerInfosDTO contenant les infos de l'usager.</li>
     *     <li>L'id du brouillon à manipuler (si présent)</li>
     *     <li>Le client de l'API XAF</li>
     * </ol>
     */
    private Object[] setup(HttpServletRequest request, HttpServletResponse response) {

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return new Object[0];
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

        LOGGER.info("DemarcheID={}, UsagerID={}, BrouillonID={}", demarcheId, usagerId, brouillonId);

        AfApiClient afApiClient = getAfApiClient();

        return new Object[]{usagerInfosDTO, brouillonId, afApiClient};
    }

    /**
     * Factorisation des méthodes PUT et POST
     *
     * @param request
     *            Requête initiale de la Servlet
     * @param response
     *            Réponse initiale de la Servlet
     * @param httpMethod
     *            Indique si l'on souhaite effectuer un POST ou un PUT
     */
    private void doHttpMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod httpMethod) {

        if (!HttpMethod.PUT.equals(httpMethod) && !HttpMethod.POST.equals(httpMethod)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Mauvais appel de méthode.");
            return;
        }

        Object[] params = setup(request, response);
        if (params.length == 0) {
            return;
        }
        UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) params[0];
        String brouillonId = (String) params[1];
        AfApiClient afApiClient = (AfApiClient) params[2];

        // Récupération du JSON reçu en input et transmission au 2ème service en UTF8
        StringBuilder buffer = new StringBuilder();
        try {
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.toString().length() == 0) {
                AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                        "Erreur: JSON manquant");
                return;
            }

            LOGGER.info("Appel à la démarche pour créer le brouillon");
            ObjectMapper mapper = new ObjectMapper();
            BrouillonDTO brouillonInput = mapper.readValue(buffer.toString(), BrouillonDTO.class);
            BrouillonDTO brouillonDto = null;
            if (HttpMethod.POST.equals(httpMethod)) {
                brouillonDto = afApiClient.creerBrouillon(brouillonInput, usagerInfosDTO.getId());
                response.setStatus(HttpStatus.SC_CREATED);
            } else {
                brouillonDto = afApiClient.updateBrouillon(brouillonInput, Integer.parseInt(brouillonId), usagerInfosDTO.getId());
                response.setStatus(HttpStatus.SC_OK);
            }

            // TODO : gestion des erreurs
            String repJson = mapper.writeValueAsString(brouillonDto);
            IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
        } catch (IOException | NumberFormatException e) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "BrouillonsServlet - Une erreur est survenue lors de l'appel à la méthode " + httpMethod.name());
            return;
        }

        response.setContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /brouillons doPost()");
        doHttpMethod(request, response, HttpMethod.POST);
        LOGGER.info("====================== Fin /brouillons doPost()");
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /brouillons doPut()");
        doHttpMethod(request, response, HttpMethod.PUT);
        LOGGER.info("====================== Fin /brouillons doPut()");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /brouillons doGet()");

        Object[] params = setup(request, response);
        if (params.length == 0) {
            return;
        }
        UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) params[0];
        String brouillonId = (String) params[1];
        AfApiClient afApiClient = (AfApiClient) params[2];

        try {
            String repJson;
            ObjectMapper mapper = new ObjectMapper();
            if (StringUtils.isBlank(brouillonId)) {
                LOGGER.info("Appel à la démarche pour récupérer tous les brouillons de l'usager");
                List<BrouillonDTO> brouillonDtos = afApiClient.getBrouillons(usagerInfosDTO.getId());
                // TODO : gestion des erreurs
                response.setStatus(HttpStatus.SC_OK);
                repJson = mapper.writeValueAsString(brouillonDtos);
            }
            else {
                LOGGER.info("Appel à la démarche pour récupérer le brouillon {}", brouillonId);
                BrouillonDTO brouillonDto = afApiClient.getBrouillon(Integer.parseInt(brouillonId), usagerInfosDTO.getId());
                // TODO : gestion des erreurs
                response.setStatus(HttpStatus.SC_OK);
                repJson = mapper.writeValueAsString(brouillonDto);
            }
            IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
        } catch (Exception e) {
            LOGGER.error("BrouillonsServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        response.setContentType(MediaType.APPLICATION_JSON);
        LOGGER.info("====================== Fin /brouillons doGet()");
    }
    
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /brouillons doDelete()");

        Object[] params = setup(request, response);
        if (params.length == 0) {
            return;
        }
        UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) params[0];
        String brouillonId = (String) params[1];
        AfApiClient afApiClient = (AfApiClient) params[2];

        LOGGER.info("Appel à la démarche pour supprimer le brouillon");
        try {
            afApiClient.deleteBrouillon(Integer.parseInt(brouillonId), usagerInfosDTO.getId());
            response.setStatus(HttpStatus.SC_OK);
        } catch (NumberFormatException e) {
            LOGGER.error("BrouillonsServlet - Une erreur est survenue lors de l'appel à la méthode DELETE", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        response.setContentType(MediaType.APPLICATION_JSON);
        LOGGER.info("====================== Fin /brouillons doDelete()");
    }
}
