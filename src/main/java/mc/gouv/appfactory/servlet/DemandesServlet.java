package mc.gouv.appfactory.servlet;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.Static;
import mc.gouv.appfactory.util.AppFactoryServletUtils;

/**
 * Servlet mettant à disposition le service /demandes avec les méthodes PUT, POST, GET, DELETE.
 * Cette servlet récupère le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS
 * correspontants dans le back-end générique.
 * 
 * @author qdeme
 *
 */
public class DemandesServlet extends HttpServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static Logger LOGGER = LoggerFactory.getLogger(DemandesServlet.class);
    
    private static final String DEMANDES_URL = Static.getValue("mc.gouv.appfactory.demarchesws.demandes.url");
    
    private enum HttpMethod {
        PUT,
        POST,
        GET,
        DELETE;
    }
    
    public HttpServletResponse doHttpMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod httpMethod) throws UnsupportedOperationException, IOException {
        
        String pathInfo = request.getPathInfo();
        String demandeId = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            demandeId = pathInfo.split("/")[1];
        }
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
        }
        else {
            UsagerInfos usagerInfos = (UsagerInfos)session.getAttribute("login");
            if (usagerInfos == null) {
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            }
            else {
                // Récupération de l'ID de l'usager
                Integer usagerId = usagerInfos.getId();
                
                // Récupération de l'ID de la démarche dans le Context-Param
                String demarcheId = getServletContext().getInitParameter("DemarcheID");
                
                LOGGER.info("DemarcheID=" + demarcheId + ", UsagerID=" + usagerId);
                
                // Transmission du JSON d'infos du compte démarche, reçu en input, dans le WS back-end
                
                // Définition de l'authentification
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, 
                    new UsernamePasswordCredentials("abc", "abc"));
                
                // GET /demandes/{demandeId} => retourne la demande spécifiée
                // GET /demandes => retourne toutes les demandes de l'usager connecté
                // POST /demandes => crée une demande pour cette démarche et cet usager
                // POST /demandes/{demandeId} => pour modifier une demande
                // DELETE /demandes/{demandeId} => pour supprimer une demande
                
                // Création du client HTTP avec la bonne adresse
                HttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
                HttpRequestBase finalRequest = null;
                String url = DEMANDES_URL;
                if (HttpMethod.POST.equals(httpMethod)) {
                    if (demandeId != null) {
                        // Modification
                        url += "/" + demarcheId + "/" + demandeId;
                    }
                    else {
                        // Création
                        url += "/" + demarcheId + "?usagerId=" + usagerId;
                    }
                    finalRequest = new HttpPost(url);
                }
                else if (HttpMethod.GET.equals(httpMethod)) {
                    if (demandeId != null) {
                        // Demande spécifique
                        url += "/" + demarcheId + "/" + demandeId;
                    }
                    else {
                        // Toutes les demandes pour cet usager et cette démarche
                        url += "/" + demarcheId + "?usagerId=" + usagerId;
                    }
                    finalRequest = new HttpGet(url);
                }
                else if (HttpMethod.DELETE.equals(httpMethod)) {
                    if (demandeId == null) {
                        response.setStatus(HttpStatus.SC_BAD_REQUEST);
                        return response;
                    }
                    url += "/" + demarcheId + "/" + demandeId;
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
        }
        
        return null;
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("/demandes doPost()");
        
        response = doHttpMethod(request, response, HttpMethod.POST);
        
        LOGGER.info("Fin /demandes doPost()");
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("/demandes doGet()");
        
        response = doHttpMethod(request, response, HttpMethod.GET);
        
        LOGGER.info("Fin /demandes doGet()");
    }
    
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("/demandes doDelete()");
        
        response =doHttpMethod(request, response, HttpMethod.DELETE);
        
        LOGGER.info("Fin /demandes doDelete()");
    }
}
