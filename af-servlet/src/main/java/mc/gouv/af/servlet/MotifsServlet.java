package mc.gouv.af.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.af.servlet.util.AppFactoryServletUtils;
import mc.gouv.af.servlet.util.AppFactoryServletUtils.ServiceTarget;

/**
 * Servlet mettant à disposition le service /motifs avec uniquement la méthode GET pour le front.
 * Cette servlet récupère le DemarcheID et appelle le WS dans le back-end générique.
 * 
 * @author qdeme
 *
 */
public class MotifsServlet extends HttpServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static Logger LOGGER = LoggerFactory.getLogger(MotifsServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /motifs doGet()");
        
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED, "Utilisateur non autorisé");
            return;
        }
        
        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);
        
        LOGGER.info("DemarcheID=" + demarcheId);
        
        // Création du client HTTP avec la bonne adresse
        HttpClient httpClient = HttpClientBuilder.create().
                setDefaultCredentialsProvider(AppFactoryServletUtils.getCredentialsProvider(ServiceTarget.DEMARCHES)).build();
        HttpRequestBase finalRequest = null;
        String url = AppFactoryServletUtils.DEM_MOTIFS_URL + "/" + demarcheId;
        finalRequest = new HttpGet(url);
        
        // Envoi de la requête
        LOGGER.info("Appel du WS Demarches: " + url);
        HttpResponse finalResponse = httpClient.execute(finalRequest);
        LOGGER.info("Code réponse : " + finalResponse.getStatusLine().getStatusCode());
        
        // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
        LOGGER.info("Constitution de la réponse pour retour au client");
        response.setContentType("application/json");
        
        response.setStatus(finalResponse.getStatusLine().getStatusCode());
        
        IOUtils.copy(finalResponse.getEntity().getContent(), response.getOutputStream());
        
        LOGGER.info("====================== Fin /motifs doGet()");
    }
}
