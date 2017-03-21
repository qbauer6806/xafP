package mc.gouv.af.servlet.util;

import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.af.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.dem.apishared.model.AccessDTO;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

public class AppFactoryServletUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(AppFactoryServletUtils.class);

    public static final String DEMARCHEID_KEY = "DemarcheID";

    public static final String CONTAINER_KEY = "ContainerID";

    public static final String CODE_MOTIF_ANNULATION_KEY = "CodeMotifAnnulation";

    public static final String FILE_METADATA_DEMANDEID = "X-MC-DEMANDEID";

    public static final String FILE_METADATA_DEMANDESTATUT = "X-MC-DEMANDESTATUT";

    public static final String CAPTCHA_TOKEN_REGEXP = "^recaptcha_([0-9.]+)_(.*)_(.*)$";

    public static final String XSRF_COOKIE = "XSRF-TOKEN";
    public static final String XSRF_HEADER = "X-XSRF-TOKEN";
    public static final String XSRF_SESSION_ATTRIBUTE = "XSRF-TOKEN";

    public enum ServiceTarget {
        DEMARCHES,
        FILE,
        MAIL
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
     * @return
     * @throws IOException
     */
    public static HttpServletResponse logAndSendError(Logger logger, HttpServletResponse response, int httpStatus,
            String errMsg, Exception e) throws IOException {
        logger.error(errMsg, e);
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.getOutputStream().write(("{ \"errors\" : [ { \"libelle\" : \"" + errMsg + "\" } ] }").getBytes());
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
     * @return
     * @throws IOException
     */
    public static HttpServletResponse logAndSendError(Logger logger, HttpServletResponse response, int httpStatus,
            String errMsg) throws IOException {
        logger.error(errMsg);
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.getOutputStream().write(("{ \"errors\" : [ { \"libelle\" : \"" + errMsg + "\" } ] }").getBytes());
        return response;
    }

    /**
     * Génère un UUID version 1 (time+location based UUID)
     * 
     * @return
     */
    public static UUID generateUUID() {
        EthernetAddress addr = EthernetAddress.fromInterface();
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(addr);
        UUID uuid = uuidGenerator.generate();
        return uuid;
    }

    /**
     * Appelle le WS Demarches de récupération d'accès afin de connaître l'AccessID en fonction du demarcheID et de
     * l'usagerID
     * 
     * @param demarcheId
     * @param usagerId
     * @return L'accessID
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws UnsupportedOperationException
     * @throws IOException
     */
    public static Integer getAccessID(String demarcheId, Integer usagerId)
            throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {

        // Création du client HTTP avec la bonne adresse
        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(getCredentialsProvider(ServiceTarget.DEMARCHES)).build();
        String url = AfServletGouvPropertiesResolver.getDemAccessUrl() + "/" + demarcheId + "/" + usagerId;
        HttpRequestBase finalRequest = new HttpGet(url);

        // Envoi de la requête
        LOGGER.info("Appel du WS Demarches: " + url);
        HttpResponse response = httpClient.execute(finalRequest);

        if (response.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
            // Loguer le résultat de la requête en cas d'erreur puis retourner null
            String content = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            LOGGER.error("Erreur : " + content);
            return null;
        }

        // Lecture de la réponse
        LOGGER.info("Lecture de la réponse...");
        ObjectMapper mapper = new ObjectMapper();
        AccessDTO access = mapper.readValue(response.getEntity().getContent(), AccessDTO.class);

        return access.getPkAccess();
    }

    /**
     * Récupère l'utilisateur logué depuis la session
     * 
     * @param request
     * @return
     */
    public static UsagerInfosDTO getLoggedUser(HttpServletRequest request) {
        // Récupère la session courante sans en créer une nouvelle
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        //Check le csrf token seulement si POST
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

        return (UsagerInfosDTO) session.getAttribute("login");
    }

    /**
     * Définition de l'authentification Utilisation d'un AuthCache puis d'un Context que l'on donne au moment de l'appel
     * au serveur, afin de faire une authentification dès la première tentative, et non dès la deuxième tentative, car
     * dans le deuxième cas, cela force à faire un retry et donc si on utilise un InputStream, étant donné qu'on ne peut
     * pas le lire deux fois, cela donnerait une NonRepeatableRequestException.
     * 
     * @param url
     *            URL du service à appeler
     * @return
     */
    public static HttpClientContext getHttpContextForAuth(URL url, ServiceTarget serviceTarget) {

        LOGGER.info("Constitution de la requête...");
        HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), "http");

        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());

        // Ajout de l'AuthCache au contexte d'exécution
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(getCredentialsProvider(serviceTarget));
        context.setAuthCache(authCache);

        return context;

    }

    /**
     * Définition de l'authentification
     * 
     * @return
     */
    public static CredentialsProvider getCredentialsProvider(ServiceTarget serviceTarget) {
        String user = null;
        String pwd = null;

        switch (serviceTarget) {
            case DEMARCHES:
                user = AfServletGouvPropertiesResolver.getDemarchesUser();
                pwd = AfServletGouvPropertiesResolver.getDemarchesPwd();
                break;

            case FILE:
                user = AfServletGouvPropertiesResolver.getFileUser();
                pwd = AfServletGouvPropertiesResolver.getFilePwd();
                break;

            case MAIL:
                user = AfServletGouvPropertiesResolver.getMailUser();
                pwd = AfServletGouvPropertiesResolver.getMailPwd();
                break;
        }

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pwd));
        return credentialsProvider;
    }

    /**
     * Effectue la vérification Captcha
     * 
     * @param token
     *            Le token Captcha
     * @return true ou false selon le résultat
     */
    public static boolean checkCaptcha(String token) {
        if (StringUtils.isBlank(token) || !isValidCaptchaToken(token)) {
            LOGGER.error("Token au mauvais format");
            return false;
        }

        // Extraction de l'adresse IP, du challenge, et de la réponse
        // Le challenge pouvait contenir plusieurs "_", mais pas la réponse
        StringTokenizer st = new StringTokenizer(token, "_");
        int count = st.countTokens() - 1;
        st.nextToken(); // skip "recaptcha"
        String ip = st.nextToken();
        String challenge = "";
        for (int i = 2; i < count; i++) {
            if (!challenge.equals("")) {
                challenge += "_" + st.nextToken();
            } else {
                challenge = st.nextToken();
            }
        }
        String response = st.nextToken();
        // Fin extraction

        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();

        // Initialisation de la clef privée pour la vérification du CAPTCHA via les properties
        reCaptcha.setPrivateKey(AfServletGouvPropertiesResolver.getCaptchaPrivateKey());
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(ip, challenge, response);

        return reCaptchaResponse.isValid();
    }

    /**
     * Vérifie que le token est conforme au format voulu
     * 
     * @param token
     * @return
     */
    private static boolean isValidCaptchaToken(String token) {
        return Pattern.compile(CAPTCHA_TOKEN_REGEXP).matcher(token).matches();
    }

}
