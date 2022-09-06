package mc.gouv.xaf.servlet.util;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;

/**
 * Classe utilitaire pour xaf-servlet
 * 
 * @author qdeme
 *
 */
public class AppFactoryServletUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(AppFactoryServletUtils.class);

    public static final String DEMARCHEID_KEY = "DemarcheID";

    public static final String CONTAINER_KEY = "ContainerID";

    public static final String CODE_MOTIF_ANNULATION_KEY = "CodeMotifAnnulation";

    public static final String FILE_METADATA_DEMANDEID = "X-MC-DEMANDEID";

    public static final String FILE_METADATA_DEMANDESTATUT = "X-MC-DEMANDESTATUT";

    public static final String FILE_METADATA_SCANEXECUTE = "X-MC-SCANEXECUTE";

    public static final String CAPTCHA_TOKEN_REGEXP = "^recaptcha_([0-9.]+)_(.*)_(.*)$";

    public static final String XSRF_COOKIE = "XSRF-TOKEN";
    public static final String XSRF_HEADER = "X-XSRF-TOKEN";
    public static final String XSRF_SESSION_ATTRIBUTE = "XSRF-TOKEN";
    
    public static final int USAGERID_OFFSET = 1000000000;

    public enum ServiceTarget {
        FILE
    }

    /**
     * Permet de loguer une erreur et d'envoyer l'erreur au client dans la foulée
     * 
     * @param logger
     *            Le logger à utiliser
     * @param response
     *            La réponse HTTP à modifier
     * @param httpStatus
     *            Le statut HTTP à renvoyer
     * @param errMsg
     *            Le message d'erreur à renvoyer
     * @param e
     *            L'exception
     * @return Reponse de la servlet
     * @throws IOException
     *             Exception Input/Output
     */
    public static HttpServletResponse logAndSendError(Logger logger, HttpServletResponse response, int httpStatus,
            String errMsg, Exception e) {
        logger.error(errMsg, e);
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        try {
            response.getOutputStream().write(("{ \"errors\" : [ { \"libelle\" : \"" + errMsg + "\" } ] }").getBytes());
        } catch (IOException ee) {
            LOGGER.error("AppFactoryServletUtils - Impossible d'écrire dans l'output steam de la réponse", ee);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    /**
     * Permet de loguer une erreur et d'envoyer l'erreur au client dans la foulée
     * 
     * @param logger
     *            Le logger à utiliser
     * @param response
     *            La réponse HTTP à modifier
     * @param httpStatus
     *            Le statut HTTP à renvoyer
     * @param errMsg
     *            Le message d'erreur à renvoyer
     * @return Réponse de la servlet
     * @throws IOException
     *             Exception Input/Output
     */
    public static HttpServletResponse logAndSendError(Logger logger, HttpServletResponse response, int httpStatus,
            String errMsg) {
        logger.error(errMsg);
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        try {
            response.getOutputStream().write(("{ \"errors\" : [ { \"libelle\" : \"" + errMsg + "\" } ] }").getBytes());
        } catch (IOException e) {
            LOGGER.error("AppFactoryServletUtils - Impossible d'écrire dans l'output steam de la réponse", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Génère un UUID version 1 (time+location based UUID)
     * 
     * @return UUID
     */
    public static UUID generateUUID() {
        EthernetAddress addr = EthernetAddress.fromInterface();
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(addr);
        UUID uuid = uuidGenerator.generate();
        return uuid;
    }

    /**
     * Récupère l'utilisateur logué depuis la session
     * Synchronized afin d'éviter de multiples récupérations de tokens Keycloak en même temps
     * pour la même page, dans le cas où un rafraîchissement est nécessaire.
     * 
     * @param request
     *            Requete récupérée par la servlet
     * @return Utilisateur logué
     */
    public static synchronized UsagerInfosDTO getLoggedUser(HttpServletRequest request) {
        // Récupère la session courante sans en créer une nouvelle
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        // Check le csrf token seulement si POST
        if (request.getMethod().equalsIgnoreCase("POST")) {
            String xsrfToken = request.getHeader(XSRF_HEADER);

            if (StringUtils.isBlank(xsrfToken)) {
                return null;
            }

            if (session.getAttribute(XSRF_SESSION_ATTRIBUTE) == null) {
                return null;
            }

            if (!StringUtils.equals(xsrfToken, session.getAttribute(XSRF_SESSION_ATTRIBUTE).toString())) {
                LOGGER.warn("Mauvais XSRF TOKEN : " + xsrfToken);
                return null;

            }
        }

        
        UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) session.getAttribute("login");
        if (usagerInfosDTO == null) {
        	return null;
        }
        
        // Si ce n'est pas un usager courrier
        if (!isUsagerCourrier(usagerInfosDTO.getId())) {
	        // Vérifier la validité des tokens
	        usagerInfosDTO = GichkeyService.checkTokens(usagerInfosDTO, false);
        }
        session.setAttribute("login", usagerInfosDTO);
        
        return usagerInfosDTO;
    }
    
    public static boolean isUsagerCourrier(Integer usagerId) {
    	return usagerId > USAGERID_OFFSET;
    }

    /**
     * Retourne le header d'authentification JWT correspondant au service à appeler
     * 
     * @param serviceTarget
     *            Service à appeler
     * @return Le header d'authentification JWT
     */
    public static String getAuthHeader(ServiceTarget serviceTarget) {

        String jwt = null;

        switch (serviceTarget) {
            case FILE:
                jwt = AfServletGouvPropertiesResolver.getFileJwt();
                break;
        }

        // Authentification JWT
        return "Bearer " + jwt;
    }
    
    public static String createXsrfToken(HttpSession session) {
        String xsrfToken = session.getId() + Calendar.getInstance().getTime();
        String xsrfTokenHash = DigestUtils.sha256Hex(xsrfToken);
        return xsrfTokenHash;
    }
    
    public static AfApiClient getAfApiClient() {
        return new AfApiClient(AfServletGouvPropertiesResolver.getApiUrl(),
                AfServletGouvPropertiesResolver.getApiJwt());
    }

}
