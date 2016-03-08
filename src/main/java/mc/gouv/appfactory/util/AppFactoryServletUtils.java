package mc.gouv.appfactory.util;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
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
import mc.gouv.demarches.api.model.AccessDTO;

public class AppFactoryServletUtils {
    
    private static Logger LOGGER = LoggerFactory.getLogger(AppFactoryServletUtils.class);
    
    public static final String ACCESSES_URL = Static.getValue("mc.gouv.appfactory.demarchesws.accesses.url");
    
    public static final String DEMANDES_URL = Static.getValue("mc.gouv.appfactory.demarchesws.demandes.url");
    
    public static final String LOGIN_REST_URL = Static.getValue("mc.gouv.appfactory.external.login.url");
    
    public static final String PAYS_URL = Static.getValue("mc.gouv.appfactory.external.pays.url");
    
    public static final String FILE_URL = Static.getValue("mc.gouv.appfactory.filews.file.url");
    
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
        
        // Définition de l'authentification
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("abc", "abc"));
        
        // Création du client HTTP avec la bonne adresse
        HttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
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

}
