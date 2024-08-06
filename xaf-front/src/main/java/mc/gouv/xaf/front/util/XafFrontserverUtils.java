package mc.gouv.xaf.front.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Classe utilitaire pour xaf-frontserver
 *
 * @author qdeme
 */
@Component
public class XafFrontserverUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(XafFrontserverUtils.class);

    public static final String DEMARCHEID_KEY = "DemarcheID";

    public static final String CONTAINER_ROOT = "ROOT";

    public static final String FILE_METADATA_DEMANDEID = "X-MC-DEMANDEID";

    public static final String FILE_METADATA_SCANEXECUTE = "X-MC-SCANEXECUTE";

    public static final String FILE_METADATA_TYPEMODELE = "X-MC-TypeModele";

    public static final String XSRF_COOKIE = "XSRF-TOKEN";
    public static final String XSRF_SESSION_ATTRIBUTE = "XSRF-TOKEN";
    public static final int USAGERID_OFFSET = 1000000000;
    private static final String POST = "POST";

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    @Autowired
    private GichkeyService gichkeyService;

    /**
     * Vérifie que les donneesExternes reçues de la part de l'utilisateur ne sont pas traffiquées, en les comparant à la référence du config.json
     * @param donneesExternesInput
     * @param donneesExternesConfig
     * @return
     */
    public boolean checkDonneesExternes(JsonNode donneesExternesInput, JsonNode donneesExternesConfig) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> donneesExternesInputMap = objectMapper.convertValue(donneesExternesInput, Map.class);
        Map<String, List<String>> donneesExternesConfigMap = objectMapper.convertValue(donneesExternesConfig, Map.class);
        // pour chaque donnée externe présente dans l'input, on vérifie si elle est présente dans les donneesexternes définies dans le config.json
        for (Entry<String, String> donneeExterneInput : donneesExternesInputMap.entrySet()) {
            boolean donneeExterneInputChecked = false;
            for (Entry<String, List<String>> donneeExterneConfig : donneesExternesConfigMap.entrySet()) {
                if (donneeExterneInput.getKey().equals(donneeExterneConfig.getKey()) && donneeExterneConfig.getValue().contains(donneeExterneInput.getValue())) {
                    donneeExterneInputChecked = true;
                    break;
                }
            }
            if (!donneeExterneInputChecked) {
                return false;
            }
        }
        return true;
    }

    public enum ServiceTarget {
        FILE
    }

    /**
     * Permet de loguer une erreur et d'envoyer l'erreur au client dans la foulée
     *
     * @param logger     Le logger à utiliser
     * @param httpStatus Le statut HTTP à renvoyer
     * @param errMsg     Le message d'erreur à renvoyer
     * @return Réponse de la servlet
     */
    public ResponseEntity logAndSendError(Logger logger, HttpStatus httpStatus, String errMsg) {
        logger.error(errMsg);
        return ResponseEntity.status(httpStatus).build();
    }


    /**
     * Permet de loguer une erreur et d'envoyer l'erreur au client dans la foulée
     *
     * @param logger Le logger à utiliser
     * @param errMsg Le message d'erreur à renvoyer
     * @return Réponse de la servlet
     */
    public ResponseEntity logAndSendError(Logger logger, int httpStatusCode, String errMsg) {
        logger.error(errMsg);
        return ResponseEntity.status(HttpStatus.valueOf(httpStatusCode)).build();
    }

    /**
     * Génère un UUID version 1 (time+location based UUID)
     *
     * @return UUID
     */
    public static UUID generateUUID() {
        EthernetAddress addr = EthernetAddress.fromInterface();
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(addr);
        return uuidGenerator.generate();
    }

    /**
     * Récupère l'utilisateur logué depuis la session
     * Synchronized afin d'éviter de multiples récupérations de tokens Keycloak en même temps
     * pour la même page, dans le cas où un rafraîchissement est nécessaire.
     *
     * @param request Requete récupérée par la servlet
     * @return Utilisateur logué
     */
    public synchronized UsagerInfosDTO getLoggedUser(HttpServletRequest request) {
        // Récupère la session courante sans en créer une nouvelle
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        // Check le csrf token seulement si POST
        if (POST.equalsIgnoreCase(request.getMethod())) {

            String xsrfToken = session.getAttribute(XSRF_SESSION_ATTRIBUTE).toString();

            if (StringUtils.isBlank(xsrfToken) || !xsrfToken.equals(session.getAttribute(XSRF_SESSION_ATTRIBUTE))) {
                LOGGER.warn("Mauvais XSRF TOKEN : {}", xsrfToken);
                return null;
            }
        }

        UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) session.getAttribute("login");
        if (usagerInfosDTO == null) {
            // #47087 - [FO] expiration - Page d'erreur furtive sur click de lien menu en FR et EN
            // Ici on invalide la session afin d'être redirigé vers la page de login lorsque les infos usager sont null coté mon guichet
            session.invalidate();
        } else {
            // Si ce n'est pas un usager courrier
            if (!isUsagerCourrier(usagerInfosDTO.getId())) {
                // Vérifier la validité des tokens
                usagerInfosDTO = gichkeyService.checkTokens(usagerInfosDTO, false);
            }
            session.setAttribute("login", usagerInfosDTO);
        }

        return usagerInfosDTO;
    }

    public static boolean isUsagerCourrier(Integer usagerId) {
        return usagerId > USAGERID_OFFSET;
    }

    /**
     * Retourne le header d'authentification JWT correspondant au service à appeler
     *
     * @param serviceTarget Service à appeler
     * @return Le header d'authentification JWT
     */
    public String getAuthHeader(ServiceTarget serviceTarget) {
        String jwt = "Bearer ";
        if (ServiceTarget.FILE.equals(serviceTarget)) {
            jwt += propertiesResolver.getFileJwt();
        }
        return jwt;
    }

    public static String createXsrfToken(HttpSession session) {
        String xsrfToken = session.getId() + Calendar.getInstance().getTime();
        return DigestUtils.sha256Hex(xsrfToken);
    }

    public AfApiClient getAfApiClient() {
        return new AfApiClient(propertiesResolver.getApiUrl(), propertiesResolver.getApiJwt());
    }

    public JsonNode getConfig() throws IOException {
        InputStream inputStream = new ClassPathResource("/static/config.json").getInputStream();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputStream, JsonNode.class);
    }

}
