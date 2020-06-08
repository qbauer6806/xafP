package mc.gouv.xaf.servlet;

import java.text.SimpleDateFormat;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;

/**
 * 
 * @author qdeme
 *
 */
public class SessionsServlet extends HttpServlet {

    private static final long serialVersionUID = -7833206552171322810L;

    private static Logger LOGGER = LoggerFactory.getLogger(SessionsServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        LOGGER.info("====================== /sessions doGet()");

        // On tente de récupérer une session existante sans en créer une
        HttpSession session = request.getSession(false);

        LOGGER.info("SESSION : " + session);
        if (session != null) {

            //https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
            //Ajout du cookie XSRF-TOKEN
            
            String xsrfValue = (String) session.getAttribute(AppFactoryServletUtils.XSRF_SESSION_ATTRIBUTE);
            if(StringUtils.isBlank(xsrfValue)){
                LOGGER.info("Aucun cookie xsrf trouvé en session");
                response.setStatus(HttpStatus.SC_NOT_FOUND);
                return;
            }
            Cookie xsrfCookie = new Cookie(AppFactoryServletUtils.XSRF_COOKIE,
                    session.getAttribute(AppFactoryServletUtils.XSRF_SESSION_ATTRIBUTE).toString());
            response.addCookie(xsrfCookie);

            // Récupération de l'objet attaché à la session
            UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) session.getAttribute("login");
            LOGGER.info("usagerInfosDTO : " + usagerInfosDTO);
            // Retour au client
            response.setContentType("application/json");
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
                mapper.writeValue(response.getOutputStream(), usagerInfosDTO);
                response.getOutputStream().flush();
            } catch (Exception e) {
                LOGGER.error("SessionsServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            // Pas de session trouvée
            LOGGER.info("Aucune session trouvée");
            response.setStatus(HttpStatus.SC_NOT_FOUND);
        }

        LOGGER.info("====================== Fin /sessions doGet()");
    }
    
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /sessions doPut()");

        // On tente de récupérer une session existante sans en créer une
        HttpSession session = request.getSession(false);
        LOGGER.info("SESSION : " + session);
        if (session != null) {
            // Récupération de l'objet attaché à la session
            UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) session.getAttribute("login");
            Integer accessId = usagerInfosDTO.getAccessId();
            LOGGER.info("usagerInfosDTO : " + usagerInfosDTO + ", userId=" + usagerInfosDTO.getId() + ", accessId=" + accessId);
            
            String serviceUrl = AfServletGouvPropertiesResolver.getLoginServiceRestUrl() + "/" + String.valueOf(usagerInfosDTO.getId());
            LOGGER.info("Calling " + serviceUrl + "...");
            Request serviceRequest = Request.Get(serviceUrl);
            serviceRequest.setHeader("Accept", "application/json");
            try {
                LOGGER.info("Appel du service ts-login...");
                HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
                int code = serviceResponse.getStatusLine().getStatusCode();
                LOGGER.info("Code retour ts-login : " + code);
                if (code == HttpServletResponse.SC_NOT_FOUND || code != HttpServletResponse.SC_OK) {
                    LOGGER.error("Erreur lors de l'appel à servicerest/rest/usagers : " + code);
                    response.setStatus(code);
                } else {
                    LOGGER.info("Stockage des informations usager dans la session...");
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.setDateFormat(new ISO8601DateFormat());

                    UsagerInfosDTO uinfos = mapper.readValue(serviceResponse.getEntity().getContent(),
                            UsagerInfosDTO.class);

                    if (uinfos != null) {
                        // Stockage de cet objet d'infos d'usager dans la session HTTP
                        session = request.getSession();
                        LOGGER.info("Réincorporer l'accessId dans la session pour protéger les appels à FILE...");
                        uinfos.setAccessId(accessId);
                        session.setAttribute("login", uinfos);
                        //https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
                        session.setAttribute(AppFactoryServletUtils.XSRF_SESSION_ATTRIBUTE, AppFactoryServletUtils.createXsrfToken(session));
                    }
                }
            } catch (Exception e) {
                AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        "Erreur interne: ", e);
            }
            
        } else {
            // Pas de session trouvée
            LOGGER.info("Aucune session trouvée");
            response.setStatus(HttpStatus.SC_NOT_FOUND);
        }

        LOGGER.info("====================== Fin /sessions doPut()");
    }

}
