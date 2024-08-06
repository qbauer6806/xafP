package mc.gouv.xaf.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import mc.gouv.xaf.front.dto.KeycloakTokenInfo;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.GichkeyService;
import mc.gouv.xaf.front.util.GichuniService;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 
 * @author qdeme
 *
 */
@Controller
@RequestMapping("/login")
public class LoginController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    private static final String LOGIN = "login";

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    @Autowired
    private GichuniService gichuniService;

    @Autowired
    private GichkeyService gichkeyService;

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @PostMapping
    public ResponseEntity<String> doPost(@RequestParam(name = "id", required = false) String sessionId,
                                         @RequestParam(required = false) String code,
                                         HttpServletRequest request) {
        LOGGER.info("====================== /login doPost()");

        // Si pas de sessionId, il se peut que le code provienne de Keycloak (GICHKEY)
        if (StringUtils.isBlank(sessionId)) {
        	sessionId = code;
        }

        String safe = sessionId != null ? sessionId.replaceAll(SharedMessages.UNSAFE_CHARS, "_") : null;
        LOGGER.info("SessionID = {}", safe);

        if (StringUtils.isBlank(sessionId)) {
            return ResponseEntity.badRequest().build();
        }

        if (!sessionId.startsWith("c_")) {
            // Le sessionId ne commence pas par "c_", donc appel du service ts-login

            LOGGER.info("<Usager classique>");
            logParams(request);
            KeycloakTokenInfo tokenInfo = gichkeyService.getTokenFromAuthCode(sessionId);
            
            if (tokenInfo != null) {
            	UsagerInfosDTO uinfos = gichkeyService.getUsagerInfosFromToken(tokenInfo);
            	tokenInfo.setDateObtention(new Date());
            	
            	// Appel à GICHUNI pour obtenir des informations de profil complémentaires
            	uinfos = gichuniService.getGichuniApiProfileData(uinfos);
            	
                // Stockage de cet objet d'infos d'usager dans la session HTTP
                HttpSession session = request.getSession();
                session.setAttribute(LOGIN, uinfos);
                session.setAttribute(XafFrontserverUtils.XSRF_SESSION_ATTRIBUTE, XafFrontserverUtils.createXsrfToken(session));
            } else {
            	LOGGER.error("Impossible d'obtenir les tokens");
                return ResponseEntity.internalServerError().build();
            }
        } else {
            // Le sessionId commence par "c_", donc il s'agit du login d'un usager courrier, Effectuer l'appel au WS de DEM

            //Vérification du sig
            HttpStatus sigStatus = checkSig(request, sessionId);
            if (sigStatus != HttpStatus.OK) {
                return ResponseEntity.status(sigStatus).build();
            }

            LOGGER.info("<Usager courrier>");

            int usagerCourrierId;
            try {
                usagerCourrierId = Integer.parseInt(sessionId.substring(2));
            } catch (NumberFormatException e) {
                LOGGER.error("Impossible de parser l'id de l'usager courrier", e);
                return ResponseEntity.internalServerError().build();
            }
            LOGGER.info("UsagerCourrierId : {}", usagerCourrierId);

            LOGGER.info("Appel de la démarche pour récupérer l'usager courrier...");
            UsagerCourrierDTO usagerCourrier = getAfApiClient().getUsagerCourrier(usagerCourrierId);

            if (usagerCourrier == null) {
                LOGGER.info("Login infructueux");
                return ResponseEntity.notFound().build();
            }

            LOGGER.info("Stockage des informations usager dans la session...");
            UsagerInfosDTO uinfos = this.getUsagerInfosDTO(usagerCourrier);
            AccessDTO accessDTO = getAfApiClient().getAccess(usagerCourrierId);
            if(accessDTO != null){
                uinfos.setAccessId(accessDTO.getPkAccess());
            }
            // Stockage de cet objet d'infos d'usager dans la session HTTP
            HttpSession session = request.getSession();
            session.setAttribute(LOGIN, uinfos);
            session.setAttribute(XafFrontserverUtils.XSRF_SESSION_ATTRIBUTE, XafFrontserverUtils.createXsrfToken(session));
        }

        LOGGER.info("====================== Fin /login doPost()");

        return ResponseEntity.ok().build();
    }

    private UsagerInfosDTO getUsagerInfosDTO(UsagerCourrierDTO usagerCourrier) {
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

        return uinfos;
    }

    @DeleteMapping
    public ResponseEntity doDelete(HttpServletRequest request) {
        LOGGER.info("====================== /login doDelete()");

        // Le SessionID est stocké dans l'URL parameter "id"
        String sessionId = request.getParameter("id");
        if (null != sessionId) {
            String safe = sessionId.replaceAll(SharedMessages.UNSAFE_CHARS, "_");
            LOGGER.info("SessionID = {}", safe);
        }

        if (StringUtils.isBlank(sessionId)) {
            LOGGER.info("Pas d'ID donné en paramètre, donc appel à GICHKEY pour faire le logout");
        	UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
            ClassicHttpResponse postResponse = (ClassicHttpResponse)gichkeyService.logout(usagerInfosDTO);
        	int statusCode = postResponse.getCode();
        	
			// Si tout s'est bien passé, alors on détruit la session côté AppFactoryServlet
			if (statusCode == HttpServletResponse.SC_NO_CONTENT) {
				LOGGER.info("Retour 204 OK No Content, destruction de la session côté af-servlet...");
				request.getSession().removeAttribute(LOGIN);
				request.getSession().invalidate();
                return ResponseEntity.ok().build();
			}
            if (postResponse.getEntity() != null) {
                try {
                    return xafFrontserverUtils.logAndSendError(LOGGER, statusCode,
                            "Erreur: GICHKEY a retourné le code " + statusCode + " ("
                                    + EntityUtils.toString(postResponse.getEntity()) + ")");
                } catch (ParseException | IOException e) {
                    LOGGER.error("Erreur lors du EntityUtils.toString()", e);
                    return ResponseEntity.internalServerError().build();
                }
            }
            return xafFrontserverUtils.logAndSendError(LOGGER, statusCode, "Erreur: GICHKEY a retourné le code " + statusCode);
        } else if (!sessionId.startsWith("c_")) {
            // Usager courrier, pas d'appel à GICHKEY pour faire un logout Juste destruction de la session
            LOGGER.info("Usager courrier : suppression de la session sans appel à GICHKEY...");
            request.getSession().removeAttribute(LOGIN);
            request.getSession().invalidate();

            LOGGER.info("====================== Fin /login doDelete()");
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.noContent().build();
    }


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

    private HttpStatus checkSig(HttpServletRequest request, String sessionId) {
        String sig = request.getParameter("sig");
        if (StringUtils.isBlank(sig)) {
            return HttpStatus.BAD_REQUEST;
        }
        String safeSig = sig.replaceAll(SharedMessages.UNSAFE_CHARS, "_");
        LOGGER.info("Vérification du sig : {}", safeSig);
        StringTokenizer strToken = new StringTokenizer(sig, ":");
        String signature = strToken.nextToken();
        String currentMilli = strToken.nextToken();
        String signatureComputed = DigestUtils.sha256Hex(propertiesResolver.getSharedKey() + sessionId + currentMilli);
        LOGGER.info("Sig calculé : {}", signatureComputed);
        if (!StringUtils.equals(signature, signatureComputed)) {
            LOGGER.info("SIGS DIFFERENT");
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.OK;
    }

}
