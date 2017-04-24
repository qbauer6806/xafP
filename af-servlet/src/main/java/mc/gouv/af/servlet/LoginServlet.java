package mc.gouv.af.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.af.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.af.servlet.util.AppFactoryServletUtils;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.UsagerCourrierDTO;

/**
 * 
 * @author qdeme
 *
 */
public class LoginServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -394488730959377371L;

    private static Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        LOGGER.info("====================== /login doPost()");

        // Le SessionID est stocké dans l'URL parameter "id"
        String sessionId = request.getParameter("id");
        String sig = request.getParameter("sig");

        LOGGER.info("SessionID = " + sessionId);

        if (StringUtils.isBlank(sessionId)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!sessionId.startsWith("c_")) {
            // Le sessionId ne commence pas par "c_", donc appel du service ts-login

            LOGGER.info("<Usager classique>");

            String serviceUrl = AfServletGouvPropertiesResolver.getLoginRestUrl() + "/" + sessionId;
            Request serviceRequest = Request.Get(serviceUrl);
            serviceRequest.setHeader("Accept", "application/json");
            try {
                LOGGER.info("Appel du service ts-login...");
                HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
                int code = serviceResponse.getStatusLine().getStatusCode();
                LOGGER.info("Code retour ts-login : " + code);
                if (code == HttpServletResponse.SC_NOT_FOUND || code != HttpServletResponse.SC_OK) {
                    LOGGER.info("Login infructueux");
                    response.setStatus(HttpStatus.SC_NOT_FOUND);
                } else {
                    LOGGER.info("Stockage des informations usager dans la session...");
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
                    UsagerInfosDTO uinfos = mapper.readValue(serviceResponse.getEntity().getContent(),
                            UsagerInfosDTO.class);

                    if (uinfos != null) {
                        // Stockage de cet objet d'infos d'usager dans la session HTTP
                        HttpSession session = request.getSession();
                        session.setAttribute("login", uinfos);
                        //https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
                        session.setAttribute(AppFactoryServletUtils.XSRF_SESSION_ATTRIBUTE, createXsrfToken(session));
                    }
                }
            } catch (Exception e) {
                response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        "Erreur interne: ", e);
            }
        } else {
            // Le sessionId commence par "c_", donc il s'agit du login d'un usager courrier
            // Effectuer l'appel au WS de DEM

            //Vérification du sig
            if (StringUtils.isBlank(sig)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            } else {
                LOGGER.info("Vérification du sig : {}", sig);
                StringTokenizer strToken = new StringTokenizer(sig, ":");
                String signature = strToken.nextToken();
                String currentMilli = strToken.nextToken();

                String signatureComputed = DigestUtils
                        .sha256Hex(AfServletGouvPropertiesResolver.getSharedKey() + sessionId + currentMilli);

                LOGGER.info("Sig calculé : {}", signatureComputed);

                if (!StringUtils.equals(signature, signatureComputed)) {
                    LOGGER.info("SIGS DIFFERENT");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

            }

            LOGGER.info("<Usager courrier>");

            Integer usagerCourrierId = Integer.parseInt(sessionId.substring(2));

            LOGGER.info("UsagerCourrierId : " + usagerCourrierId);

            // Création du DemClient
            DemClient dc = getDemClient();

            // Récupération de l'ID de la démarche dans le Context-Param
            String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);

            LOGGER.info("Appel du service DEM...");
            UsagerCourrierDTO usagerCourrier = dc.getUsagerCourrier(demarcheId, usagerCourrierId);

            if (usagerCourrier == null) {
                LOGGER.info("Login infructueux");
                response.setStatus(HttpStatus.SC_NOT_FOUND);
            } else {
                LOGGER.info("Stockage des informations usager dans la session...");
                UsagerInfosDTO uinfos = new UsagerInfosDTO();
                uinfos.setAdresse1(usagerCourrier.getAdresse1());
                uinfos.setAdresse2(usagerCourrier.getAdresse2());
                uinfos.setCodePostal(usagerCourrier.getCodePostal());
                uinfos.setComplementAdresse(usagerCourrier.getAdresseComplement());
                // uinfos.setDateActivation(), que mettre ?
                uinfos.setDateCreation(usagerCourrier.getDateCreation());
                // uinfos.setDateDerConnexion(), que mettre ?
                uinfos.setEmail(usagerCourrier.getEmail());
                // uinfos.setEtat(), que mettre ?
                uinfos.setId(usagerCourrier.getPkUsagersCourrier());
                uinfos.setLogin(StringUtils.defaultString(usagerCourrier.getPrenom()) + " " + usagerCourrier.getNom()
                        + " (courrier)");
                uinfos.setNom(usagerCourrier.getNom());
                uinfos.setPaysCode(usagerCourrier.getPays());
                uinfos.setPrenom(usagerCourrier.getPrenom());
                uinfos.setRaisonSociale(usagerCourrier.getRaisonSociale());
                if (usagerCourrier.getTitre() != null) {
                    uinfos.setTitre(usagerCourrier.getTitre().shortValue());
                }

                uinfos.setVille(usagerCourrier.getVille());
                uinfos.setUsagerCourrier(true);
                // Stockage de cet objet d'infos d'usager dans la session HTTP
                HttpSession session = request.getSession();
                session.setAttribute("login", uinfos);
                session.setAttribute(AppFactoryServletUtils.XSRF_SESSION_ATTRIBUTE, createXsrfToken(session));
            }
        }

        LOGGER.info("====================== Fin /login doPost()");

    }

    private String createXsrfToken(HttpSession session) {
        String xsrfToken = session.getId() + Calendar.getInstance().getTime();
        String xsrfTokenHash = DigestUtils.sha256Hex(xsrfToken);
        return xsrfTokenHash;
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        LOGGER.info("====================== /login doDelete()");

        // Le SessionID est stocké dans l'URL parameter "id"
        String sessionId = request.getParameter("id");

        LOGGER.info("SessionID = " + sessionId);

        if (StringUtils.isBlank(sessionId)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!sessionId.startsWith("c_")) {
            // Le sessionId ne commence pas par "c_", donc appel du service ts-login

            String serviceUrl = AfServletGouvPropertiesResolver.getLoginRestUrl() + "/" + sessionId;
            Request serviceRequest = Request.Delete(serviceUrl);
            try {
                LOGGER.info("Appel du service ts-login...");
                HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
                int statusCode = serviceResponse.getStatusLine().getStatusCode();
                response.setStatus(statusCode);
                // Si tout s'est bien passé, alors on détruit la session côté AppFactoryServlet
                if (statusCode == HttpServletResponse.SC_NO_CONTENT) {
                    request.getSession().removeAttribute("login");
                    request.getSession().invalidate();
                } else {
                    if (serviceResponse.getEntity() != null) {
                        response = AppFactoryServletUtils.logAndSendError(LOGGER, response, statusCode,
                                "Erreur: le service ts-login a retourné le code " + statusCode + " ("
                                        + EntityUtils.toString(serviceResponse.getEntity()) + ")");
                    } else {
                        response = AppFactoryServletUtils.logAndSendError(LOGGER, response, statusCode,
                                "Erreur: le service ts-login a retourné le code " + statusCode);
                    }
                }
            } catch (Exception e) {
                response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        "Erreur interne: ", e);
            }
        } else {
            // Usager courrier, pas d'appel à DEM pour faire un logout
            // Juste destruction de la session

            LOGGER.info("Usager courrier : suppression de la session sans appel à DEM...");

            request.getSession().removeAttribute("login");
            request.getSession().invalidate();

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

        LOGGER.info("====================== Fin /login doDelete()");

    }

}
