package mc.gouv.xaf.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;

import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;

/**
 * 
 * Servlet mettant à disposition le service /accesses avec les méthodes PUT, POST, GET, DELETE. Cette servlet récupère
 * le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS correspontants dans le back-end générique.
 * 
 * @author qdeme
 *
 */
public class AccessesServlet extends AbstractAfServlet {

    private static final long serialVersionUID = 520893456441444275L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessesServlet.class);

    private UsagerInfosDTO getUsagerId(HttpServletRequest request, HttpServletResponse response) {
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return null;
        }
        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();
        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);
        LOGGER.info("DemarcheID={}, UsagerID={}", demarcheId, usagerId);
        return usagerInfosDTO;
    }

    /**
     * Traitement des méthodes POST
     *
     * @param request
     *            Requête initiale de la Servlet
     * @param response
     *            Réponse initiale de la Servlet
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /accesses doPost()");
        UsagerInfosDTO usagerInfosDTO = getUsagerId(request, response);
        if (null == usagerInfosDTO) {
            return;
        }

        try {
            // Récupération du JSON reçu en input et transmission au 2ème service
            StringBuilder buffer = new StringBuilder();
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
            ObjectMapper mapper = new ObjectMapper();
            AccessInputDTO accessInput = mapper.readValue(buffer.toString(), AccessInputDTO.class);
            LOGGER.info("Appel à la démarche pour créer l'accès...");
            AccessDTO access = getAfApiClient().createOrUpdateAccess(usagerInfosDTO.getId(), accessInput);

            String repJson = mapper.writeValueAsString(access);
            response.setContentType(MediaType.APPLICATION_JSON);
            if (repJson != null) {
                IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
            }
        } catch (IOException e) {
            LOGGER.error("AccessesServlet - Une erreur est survenue lors de l'appel à la méthode POST", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /accesses doPost()");
    }

    /**
     * Traitement des méthodes GET
     *
     * @param request
     *            Requête initiale de la Servlet
     * @param response
     *            Réponse initiale de la Servlet
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /accesses doGet()");
        UsagerInfosDTO usagerInfosDTO = getUsagerId(request, response);
        if (null == usagerInfosDTO) {
            return;
        }

        try {
            LOGGER.info("Appel à la démarche pour récupérer l'accès...");
            AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
            LOGGER.info("Incorporer l'AccessID dans la session pour protéger les appels à FILE... accessId={}", access.getPkAccess());
            HttpSession session = request.getSession();
            usagerInfosDTO.setAccessId(access.getPkAccess());
            session.setAttribute("login", usagerInfosDTO);

            ObjectMapper mapper = new ObjectMapper();
            String repJson = mapper.writeValueAsString(access);
            response.setContentType(MediaType.APPLICATION_JSON);
            if (repJson != null) {
                IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
            }
        } catch (IOException e) {
            LOGGER.error("AccessesServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        LOGGER.info("====================== Fin /accesses doGet()");
    }

    /**
     * Traitement des méthodes DELETE
     *
     * @param request
     *            Requête initiale de la Servlet
     * @param response
     *            Réponse initiale de la Servlet
     */
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /accesses doDelete()");
        UsagerInfosDTO usagerInfosDTO = getUsagerId(request, response);
        if (null == usagerInfosDTO) {
            return;
        }

        LOGGER.info("Appel de la démarche pour désinscrire l'usager...");
        String langue = request.getParameter("langue");
        getAfApiClient().desinscriptionUsager(usagerInfosDTO.getId(), langue);

        LOGGER.info("Inclure la réponse dans le HttpServletResponse...");
        response.setStatus(HttpStatus.SC_OK);

        LOGGER.info("====================== Fin /accesses doDelete()");
    }
}
