package mc.gouv.appfactory.servlet;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.appfactory.dto.UsagerInfosDTO;
import mc.gouv.appfactory.util.AppFactoryServletUtils;
import mc.gouv.appfactory.util.AppFactoryServletUtils.ServiceTarget;

public class FileDownloadServlet extends HttpServlet {
    
    private static final long serialVersionUID = -2464829773835748491L;
    
    private static Logger LOGGER = LoggerFactory.getLogger(FileDownloadServlet.class);
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /filedownload doGet()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null || !AppFactoryServletUtils.goodIp(request)) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED, "Utilisateur non autorisé");
            return;
        }
        
        // Récupération du nom du fichier à récupérer        
        String pathInfo = request.getPathInfo();
        String filename = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            // Format: /uuid/filename
            filename = pathInfo.split("/")[1] + "/" + pathInfo.split("/")[2];
        }
        
        if (StringUtils.isBlank(filename)) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST, "Erreur: nom du fichier manquant");
            return;
        }
        
        try {

            String appFactoryId = getServletContext().getInitParameter(AppFactoryServletUtils.APPFACTORYID_KEY);
            String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);
            
            LOGGER.debug("AppFactoryID = {}, DemarcheID = {}", appFactoryId, demarcheId);
            
            // Récupération de l'AccessID via appel WS à Demarches
            LOGGER.info("Récupération de l'AccessID correspondant");
            Integer accessId = AppFactoryServletUtils.getAccessID(demarcheId, usagerInfosDTO.getId());
            LOGGER.debug("AccessID = {}", accessId);
            
            
            if (accessId == null) {
                response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_NOT_FOUND, "Erreur: impossible de récupérer l'accès");
                return;
            }
            
            // Constitution du chemin virtuel du fichier
            // /appfactory/demarcheId/accessId/UUID/nomDuFichier
            String virtualPath = "/" + appFactoryId + "/" + demarcheId + "/" + accessId + "/" + filename;
            LOGGER.info("Chemin virtuel : {}", virtualPath);
            
            // Constitution de l'URL d'appel
            URL url = new URL(AppFactoryServletUtils.FILE_URL + virtualPath);
            LOGGER.info("URL d'appel : {}", url);
            
            // Constitution de la requête
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(url.toString());
            
            LOGGER.info("Appel du WS FILE");
            HttpResponse getResponse = client.execute(getRequest, AppFactoryServletUtils.getHttpContextForAuth(url, ServiceTarget.FILE));
            
            LOGGER.info("Constitution de la réponse pour retour au client");
            IOUtils.copy(getResponse.getEntity().getContent(), response.getOutputStream());
            response.setStatus(getResponse.getStatusLine().getStatusCode());
            response.setContentType(getResponse.getEntity().getContentType().getValue());
            // Ajout de la métadonnée indiquant le demandeId lié
            for (Header header : getResponse.getAllHeaders()) {
                if (header.getName().startsWith(AppFactoryServletUtils.FILE_METADATA_DEMANDEID)) {
                    response.addHeader(header.getName(), header.getValue());
                }
            }
            
        }
        catch (Exception e) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Erreur interne: " + e.toString() + " / " + e.getMessage());
        }
        
        LOGGER.info("====================== Fin /filedownload doGet()");
        
    }

}
