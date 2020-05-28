package mc.gouv.xaf.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import mc.gouv.xapi.error.exception.client.UnauthorizedWebException;

/**
 * Servlet mettant à disposition le service /accesses avec les méthodes PUT, POST, GET, DELETE. Cette servlet récupère
 * le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS correspontants dans le back-end générique.
 * 
 * @author qdeme
 *
 */
public class AccessesServlet extends AbstractAfServlet {

    private static final long serialVersionUID = 520893456441444275L;

    private static Logger LOGGER = LoggerFactory.getLogger(AccessesServlet.class);

    private enum HttpMethod {
        PUT,
        POST,
        GET,
        DELETE;
    }

    /**
     * D'un point de vue de l'implémentation et de la transmission au WS Demarche, les méthodes PUT et POST sont quasi
     * identiques, d'où cette factorisation
     * 
     * @param request
     *            Requête initiale de la Servlet
     * @param response
     *            Réponse initiale de la Servlet
     * @param httpMethod
     *            Indique si l'on souhaite effectuer un POST ou un PUT
     * @return La réponse que la Servlet doit transmettre
     * @throws UnsupportedOperationException
     *             L'opération n'est pas supportée
     * @throws IOException
     *             Exception Input/Output
     */
    public HttpServletResponse doHttpMethod(HttpServletRequest request, HttpServletResponse response,
            HttpMethod httpMethod) throws UnsupportedOperationException, IOException {

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);

        if (usagerInfosDTO == null) {
            return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
        }

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();

        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);

        LOGGER.info("DemarcheID=" + demarcheId + ", UsagerID=" + usagerId);

        String repJson = null;

        if (HttpMethod.POST.equals(httpMethod)) {

            // Récupération du JSON reçu en input et transmission au 2ème service
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

            ObjectMapper mapper = new ObjectMapper();
            AccessInputDTO accessInput = mapper.readValue(buffer.toString(), AccessInputDTO.class);

            LOGGER.info("Appel à la démarche pour créer l'accès...");

            AccessDTO access = getAfApiClient().createOrUpdateAccess(usagerId, accessInput);

            LOGGER.info("Inclure la réponse dans le HttpServletResponse...");

            response.setStatus(HttpStatus.SC_OK);
            repJson = mapper.writeValueAsString(access);

        } else if (HttpMethod.GET.equals(httpMethod)) {

            LOGGER.info("Appel à la démarche pour récupérer l'accès...");

            AccessDTO access = getAfApiClient().getAccess(usagerId);
            
            LOGGER.info("Incorporer l'AccessID dans la session pour protéger les appels à FILE... accessId=" + access.getPkAccess());
            HttpSession session = request.getSession();
            usagerInfosDTO.setAccessId(access.getPkAccess());
            session.setAttribute("login", usagerInfosDTO);

            LOGGER.info("Inclure la réponse dans le HttpServletResponse...");

            response.setStatus(HttpStatus.SC_OK);
            ObjectMapper mapper = new ObjectMapper();
            repJson = mapper.writeValueAsString(access);

        } else if (HttpMethod.DELETE.equals(httpMethod)) {

            // Si en DELETE, cela signifie que l'usager se désinscrit
            // Dans ce cas, appeler la démarche concernée (exemple : HAB)

            LOGGER.info("Appel de la démarche pour désinscrire l'usager...");

            try {
                String langue = request.getParameter("langue");
                getAfApiClient().desinscriptionUsager(usagerId, langue);
                response.setStatus(HttpStatus.SC_OK);
            } catch (UnauthorizedWebException e) {
                LOGGER.info("Erreur lors de la désinscription : unauthorized");
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            }

        }

        if (!HttpMethod.DELETE.equals(httpMethod)) {
            response.setContentType("application/json");
            IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
        }

        return response;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /accesses doPost()");

        response = doHttpMethod(request, response, HttpMethod.POST);

        LOGGER.info("====================== Fin /accesses doPost()");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /accesses doGet()");

        response = doHttpMethod(request, response, HttpMethod.GET);

        LOGGER.info("====================== Fin /accesses doGet()");
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        LOGGER.info("====================== /accesses doDelete()");

        response = doHttpMethod(request, response, HttpMethod.DELETE);

        LOGGER.info("====================== Fin /accesses doDelete()");
    }
}
