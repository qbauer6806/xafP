package mc.gouv.af.servlet;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.af.servlet.util.AppFactoryServletUtils;
import mc.gouv.af.servlet.util.AppFactoryServletUtils.ServiceTarget;

/**
 * Servlet mettant à disposition le service /accesses avec les méthodes PUT, POST, GET, DELETE.
 * Cette servlet récupère le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS
 * correspontants dans le back-end générique.
 * 
 * @author qdeme
 *
 */
public class AccessesServlet extends HttpServlet {

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
     * @param request Requête initiale de la Servlet
     * @param response Réponse initiale de la Servlet
     * @param httpMethod Indique si l'on souhaite effectuer un POST ou un PUT
     * @return La réponse que la Servlet doit transmettre
     * @throws UnsupportedOperationException
     * @throws IOException
     */
    public HttpServletResponse doHttpMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod httpMethod) throws UnsupportedOperationException, IOException {

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED, "Utilisateur non autorisé");
        }
        
        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();
        
        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);
        
        LOGGER.info("DemarcheID=" + demarcheId + ", UsagerID=" + usagerId);
        
        // Transmission du JSON d'infos du compte démarche, reçu en input, dans le WS back-end
        
        // Création du client HTTP avec la bonne adresse
        HttpClient httpClient = HttpClientBuilder.create().
                setDefaultCredentialsProvider(AppFactoryServletUtils.getCredentialsProvider(ServiceTarget.DEMARCHES)).build();
        HttpRequestBase finalRequest = null;
        String url = AppFactoryServletUtils.DEM_ACCESSES_URL + "/" + demarcheId + "/" + usagerId;
        if (HttpMethod.POST.equals(httpMethod)) {
            finalRequest = new HttpPost(url);
        }
        else if (HttpMethod.GET.equals(httpMethod)) {
            finalRequest = new HttpGet(url);
        }
        else if (HttpMethod.DELETE.equals(httpMethod)) {
            // Le hashPassword est stocké dans l'URL
            String hashedPassword = request.getParameter("hashedPassword");
            url += "?hashedPassword=" + hashedPassword;
            finalRequest = new HttpDelete(url);
        }
        
        if (HttpMethod.POST.equals(httpMethod)) {
            finalRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
            
            // Récupération du JSON reçu en input et transmission au 2ème service en UTF8
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            
            if (buffer.toString().length() == 0) {
                return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST, "Erreur: JSON manquant");
            }
            
            StringEntity input = new StringEntity(buffer.toString(),"UTF-8");
            
            ((HttpEntityEnclosingRequestBase)finalRequest).setEntity(input);
        }
        
        // Envoi de la requête
        LOGGER.info("Appel du WS Demarches: " + url);
        HttpResponse finalResponse = httpClient.execute(finalRequest);
        LOGGER.info("Code réponse : " + finalResponse.getStatusLine().getStatusCode());
        
        // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
        LOGGER.info("Constitution de la réponse pour retour au client");
        response.setContentType("application/json");
        
        response.setStatus(finalResponse.getStatusLine().getStatusCode());
        
        IOUtils.copy(finalResponse.getEntity().getContent(), response.getOutputStream());
        
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
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /accesses doDelete()");
        
        response = doHttpMethod(request, response, HttpMethod.DELETE);
        
        LOGGER.info("====================== Fin /accesses doDelete()");
    }
}
