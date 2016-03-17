package mc.gouv.appfactory.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.appfactory.dto.FileUploadResponseDTO;
import mc.gouv.appfactory.dto.UsagerInfosDTO;
import mc.gouv.appfactory.util.AppFactoryServletUtils;
import mc.gouv.appfactory.util.AppFactoryServletUtils.ServiceTarget;

@MultipartConfig
public class FileUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 484237515919955392L;
    
    private static Logger LOGGER = LoggerFactory.getLogger(FileUploadServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /fileupload doPost()");
        
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return;
        }
        
        // Récupération du nom du fichier à envoyer        
        String pathInfo = request.getPathInfo();
        String filename = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            filename = pathInfo.split("/")[1];
        }
        
        if (StringUtils.isBlank(filename)) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST, "Erreur: nom du fichier manquant");
            return;
        }
        
        try {
        
        // Génération de l'UUID
        UUID uuid = AppFactoryServletUtils.generateUUID();
        LOGGER.debug("UUID généré : {}", uuid.toString());

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
        String virtualPath = "/" + appFactoryId + "/" + demarcheId + "/" + accessId + "/" + uuid + "/" + filename;
        LOGGER.info("Chemin virtuel : {}", virtualPath);
        
        // Constitution de l'URL d'appel
        URL url = new URL(AppFactoryServletUtils.FILE_URL + virtualPath);
        LOGGER.info("URL d'appel : {}", url);
        
        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        Part part = request.getParts().iterator().next();
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("data", new InputStreamBody(part.getInputStream(), ContentType.DEFAULT_BINARY, "data"));
        HttpEntity multipart = builder.build();
        HttpPost postRequest = new HttpPost(url.toString());
        postRequest.setEntity(multipart);
        
        LOGGER.info("Appel du WS FILE");
        HttpResponse postResponse = client.execute(postRequest, AppFactoryServletUtils.getHttpContextForAuth(url, ServiceTarget.FILE));

        // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
        LOGGER.info("Constitution de la réponse pour retour au client");
        response.setContentType("application/json");
        
        int statusCode = postResponse.getStatusLine().getStatusCode();
        response.setStatus(statusCode);
        
        if (statusCode == HttpServletResponse.SC_OK || statusCode == HttpServletResponse.SC_CREATED) {
            // Si tout s'est bien passé, alors on forme une réponse différente que celle qui nous est retournée par FILE
            response.setContentType("application/json");
            ObjectMapper mapper = new ObjectMapper();
            // Répondre uuid/nomDuFichier
            FileUploadResponseDTO responseObj = new FileUploadResponseDTO(uuid + "/" + filename);
            String responseStr = mapper.writeValueAsString(responseObj);
            response.getOutputStream().write(responseStr.getBytes());
        }
        else {
            // S'il y a eu un problème, alors on retourne le message d'erreur au client
            IOUtils.copy(postResponse.getEntity().getContent(), response.getOutputStream());
        }
        
        }
        catch (Exception e) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Erreur interne: " + e.toString() + " / " + e.getMessage());
        }
        
        LOGGER.info("====================== Fin /fileupload doPost()");
    }
    
}
