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

import mc.gouv.Static;
import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.dem.apishared.model.AccessDTO;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

public class AppFactoryServletUtils {
    
    private static Logger LOGGER = LoggerFactory.getLogger(AppFactoryServletUtils.class);
    
    public static final String ACCESSES_URL = Static.getValue("mc.gouv.appfactory.demarchesws.accesses.url");
    
    public static final String DEMANDES_URL = Static.getValue("mc.gouv.appfactory.demarchesws.demandes.url");
    
    public static final String LOGIN_REST_URL = Static.getValue("mc.gouv.appfactory.external.login.url");
    
    public static final String PAYS_URL = Static.getValue("mc.gouv.appfactory.external.pays.url");
    
    public static final String FILE_URL = Static.getValue("mc.gouv.appfactory.filews.file.url");
    
    public static final String MAIL_URL = Static.getValue("mc.gouv.appfactory.mailws.mail.url");
    
    public static final String DEMARCHEID_KEY = "DemarcheID";
    
    public static final String APPFACTORYID_KEY = "AppFactoryID";
    
    public static final String FILE_USER = Static.getValue("mc.gouv.appfactory.filews.user");
    
    public static final String FILE_PWD = Static.getValue("mc.gouv.appfactory.filews.pwd");
    
    public static final String MAIL_USER = Static.getValue("mc.gouv.appfactory.mailws.user");
    
    public static final String MAIL_PWD = Static.getValue("mc.gouv.appfactory.mailws.pwd");
    
    public static final String DEMARCHES_USER = Static.getValue("mc.gouv.appfactory.demarchesws.user");
    
    public static final String DEMARCHES_PWD = Static.getValue("mc.gouv.appfactory.demarchesws.pwd");
    
    public static final String FILE_METADATA_DEMANDEID = "X-MC-DEMANDEID";
    
    public static final String CAPTCHA_TOKEN_REGEXP = "^recaptcha_([0-9.]+)_(.*)_(.*)$";
    
    public static final String CAPTCHA_PRIVATE_KEY = Static.getValue("mc.gouv.appfactory.captcha.privatekey");
    
    public static final String GOUV_CONTACT_EMAIL = Static.getValue("mc.gouv.appfactory.mailws.gouvemail");
    
    public enum ServiceTarget {
        DEMARCHES,
        FILE,
        MAIL
    }
    
    /**
     * Permet de loguer une erreur et d'envoyer l'erreur au client dans la foulée
     * @param logger Le logger à utiliser
     * @param response La réponse HTTP à modifier
     * @param httpStatus Le statut HTTP à renvoyer
     * @param errMsg Le message d'erreur à renvoyer
     * @return
     * @throws IOException
     */
    public static HttpServletResponse logAndSendError(Logger logger, HttpServletResponse response, int httpStatus, String errMsg) throws IOException {
        logger.error(errMsg);
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.getOutputStream().write(("{ \"errors\" : [ { \"libelle\" : \"" + errMsg + "\" } ] }").getBytes());
        return response;
    }
    
    /**
     * Génère un UUID version 1 (time+location based UUID)
     * @return
     */
    public static UUID generateUUID() {
        EthernetAddress addr = EthernetAddress.fromInterface();
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(addr);
        UUID uuid = uuidGenerator.generate();
        return uuid;
    }
    
    /**
     * Appelle le WS Demarches de récupération d'accès afin de connaître l'AccessID en fonction du demarcheID et de l'usagerID
     * @param demarcheId
     * @param usagerId
     * @return L'accessID
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws UnsupportedOperationException
     * @throws IOException
     */
    public static Integer getAccessID(String demarcheId, Integer usagerId) throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
        
        // Création du client HTTP avec la bonne adresse
        HttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(getCredentialsProvider(ServiceTarget.DEMARCHES)).build();
        String url = ACCESSES_URL + "/" + demarcheId + "/" + usagerId;
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
     * @param request
     * @return
     */
    public static UsagerInfosDTO getLoggedUser(HttpServletRequest request) {
        // Récupère la session courante sans en créer une nouvelle
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (UsagerInfosDTO)session.getAttribute("login");
    }
    
    /**
     * Définition de l'authentification
     * Utilisation d'un AuthCache puis d'un Context que l'on donne au moment de l'appel au serveur, afin de faire
     * une authentification dès la première tentative, et non dès la deuxième tentative, car dans le deuxième cas,
     * cela force à faire un retry et donc si on utilise un InputStream, étant donné qu'on ne peut pas le lire deux fois,
     * cela donnerait une NonRepeatableRequestException.
     * @param url URL du service à appeler
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
     * @return
     */
    public static CredentialsProvider getCredentialsProvider(ServiceTarget serviceTarget) {
        String user = null;
        String pwd = null;
        
        switch (serviceTarget) {
            case DEMARCHES:
                user = DEMARCHES_USER;
                pwd = DEMARCHES_PWD;
                break;
                
            case FILE:
                user = FILE_USER;
                pwd = FILE_PWD;
                break;
                
            case MAIL:
                user = MAIL_USER;
                pwd = MAIL_PWD;
                break;
        }
        
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pwd));
        return credentialsProvider;
    }
    
    /**
     * Effectue la vérification Captcha
     * @param token Le token Captcha
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
        int count = st.countTokens()-1;
        st.nextToken(); // skip "recaptcha"
        String ip = st.nextToken();
        String challenge = "";
        for (int i = 2; i < count; i++) {
            if (!challenge.equals("")) {
                challenge += "_" + st.nextToken();
            }
            else {
                challenge = st.nextToken();
            }
        }
        String response = st.nextToken();
        // Fin extraction
        
        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        
        // Initialisation de la clef privée pour la vérification du CAPTCHA via les properties
        reCaptcha.setPrivateKey(AppFactoryServletUtils.CAPTCHA_PRIVATE_KEY);
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(ip, challenge, response);
        
        return reCaptchaResponse.isValid();
    }
    
    /**
     * Vérifie que le token est conforme au format voulu
     * @param token
     * @return
     */
    private static boolean isValidCaptchaToken(String token) {
        return Pattern.compile(CAPTCHA_TOKEN_REGEXP).matcher(token).matches();
    }

}
