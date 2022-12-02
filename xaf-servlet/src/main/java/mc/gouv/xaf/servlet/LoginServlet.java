package mc.gouv.xaf.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.xaf.servlet.dto.KeycloakTokenInfo;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.util.GichkeyService;
import mc.gouv.xaf.servlet.util.GichuniService;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;

/**
 * 
 * @author qdeme
 *
 */
public class LoginServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -394488730959377371L;

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);
    private static final String LOGIN = "login";

    private void logParams(HttpServletRequest request) {
        LOGGER.info("RemoteAddr : {}", request.getRemoteAddr());
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                LOGGER.info("Header: {} = {}", header, request.getHeader(header));
            }
        }
        LOGGER.info("Scheme : {}", request.getScheme());
        LOGGER.info("ServerName : {}", request.getServerName());
        LOGGER.info("ServerPort : {}", request.getServerPort());
        LOGGER.info("isSecure : {}", request.isSecure());
    }

    private boolean checkSig(HttpServletRequest request, HttpServletResponse response, String sessionId) {
        String sig = request.getParameter("sig");
        if (StringUtils.isBlank(sig)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        } else {
            LOGGER.info("Vérification du sig : {}", sig);
            StringTokenizer strToken = new StringTokenizer(sig, ":");
            String signature = strToken.nextToken();
            String currentMilli = strToken.nextToken();
            String signatureComputed = DigestUtils.sha256Hex(AfServletGouvPropertiesResolver.getSharedKey() + sessionId + currentMilli);
            LOGGER.info("Sig calculé : {}", signatureComputed);
            if (!StringUtils.equals(signature, signatureComputed)) {
                LOGGER.info("SIGS DIFFERENT");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
        }
        return true;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /login doPost()");

        // Le SessionID est stocké dans l'URL parameter "id"
        String sessionId = request.getParameter("id");

        // Si pas de sessionId, il se peut que le code provienne de Keycloak (GICHKEY)
        if (StringUtils.isBlank(sessionId)) {
        	sessionId = request.getParameter("code");
        }

        LOGGER.info("SessionID = {}", sessionId);

        if (StringUtils.isBlank(sessionId)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!sessionId.startsWith("c_")) {
            // Le sessionId ne commence pas par "c_", donc appel du service ts-login

            LOGGER.info("<Usager classique>");
            logParams(request);
            KeycloakTokenInfo tokenInfo = GichkeyService.getTokenFromAuthCode(sessionId);
            
            if (tokenInfo != null) {
            	UsagerInfosDTO uinfos = GichkeyService.getUsagerInfosFromToken(tokenInfo);
            	tokenInfo.setDateObtention(new Date());
            	
            	// Appel à GICHUNI pour obtenir des informations de profil complémentaires
            	uinfos = GichuniService.getGichuniApiProfileData(uinfos);
            	
                // Stockage de cet objet d'infos d'usager dans la session HTTP
                HttpSession session = request.getSession();
                session.setAttribute(LOGIN, uinfos);
                session.setAttribute(AppFactoryServletUtils.XSRF_SESSION_ATTRIBUTE, AppFactoryServletUtils.createXsrfToken(session));
            } else {
            	LOGGER.error("Impossible d'obtenir les tokens");
            	response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            // Le sessionId commence par "c_", donc il s'agit du login d'un usager courrier, Effectuer l'appel au WS de DEM

            //Vérification du sig
            if (!checkSig(request, response, sessionId)) {
                return;
            }

            LOGGER.info("<Usager courrier>");

            int usagerCourrierId;
            try {
                usagerCourrierId = Integer.parseInt(sessionId.substring(2));
            } catch (NumberFormatException e) {
                LOGGER.error("Impossible de parser l'id de l'usager courrier", e);
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            LOGGER.info("UsagerCourrierId : {}", usagerCourrierId);

            LOGGER.info("Appel de la démarche pour récupérer l'usager courrier...");
            UsagerCourrierDTO usagerCourrier = getAfApiClient().getUsagerCourrier(usagerCourrierId);

            if (usagerCourrier == null) {
                LOGGER.info("Login infructueux");
                response.setStatus(HttpStatus.SC_NOT_FOUND);
                return;
            }

            LOGGER.info("Stockage des informations usager dans la session...");
            UsagerInfosDTO uinfos = new UsagerInfosDTO();
            uinfos.setAdresse1(usagerCourrier.getAdresse1());
            uinfos.setAdresse2(usagerCourrier.getAdresse2());
            uinfos.setCodePostal(usagerCourrier.getCodePostal());
            uinfos.setComplementAdresse(usagerCourrier.getAdresseComplement());
            uinfos.setEmail(usagerCourrier.getEmail());
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
            session.setAttribute(LOGIN, uinfos);
            session.setAttribute(AppFactoryServletUtils.XSRF_SESSION_ATTRIBUTE, AppFactoryServletUtils.createXsrfToken(session));
        }

        LOGGER.info("====================== Fin /login doPost()");
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /login doDelete()");

        // Le SessionID est stocké dans l'URL parameter "id"
        String sessionId = request.getParameter("id");
        LOGGER.info("SessionID = {}", sessionId);

        if (StringUtils.isBlank(sessionId)) {
            // Pas d'ID donné en paramètre, donc appel à GICHKEY pour faire le logout
        	UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        	HttpResponse postResponse = GichkeyService.logout(usagerInfosDTO);
        	int statusCode = postResponse.getStatusLine().getStatusCode();
        	
			// Si tout s'est bien passé, alors on détruit la session côté AppFactoryServlet
			if (statusCode == HttpServletResponse.SC_NO_CONTENT) {
				LOGGER.info("Retour 204 OK No Content, destruction de la session côté af-servlet...");
				request.getSession().removeAttribute(LOGIN);
				request.getSession().invalidate();
			} else {
				if (postResponse.getEntity() != null) {
					try {
						AppFactoryServletUtils.logAndSendError(LOGGER, response, statusCode,
								"Erreur: GICHKEY a retourné le code " + statusCode + " ("
										+ EntityUtils.toString(postResponse.getEntity()) + ")");
					} catch (ParseException | IOException e) {
						LOGGER.error("Erreur lors du EntityUtils.toString()", e);
					}
				} else {
					AppFactoryServletUtils.logAndSendError(LOGGER, response, statusCode,
							"Erreur: GICHKEY a retourné le code " + statusCode);
				}
			}
        } else if (!sessionId.startsWith("c_")) {
            // Usager courrier, pas d'appel à GICHKEY pour faire un logout Juste destruction de la session
            LOGGER.info("Usager courrier : suppression de la session sans appel à GICHKEY...");
            request.getSession().removeAttribute(LOGIN);
            request.getSession().invalidate();
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

        LOGGER.info("====================== Fin /login doDelete()");
    }

}
