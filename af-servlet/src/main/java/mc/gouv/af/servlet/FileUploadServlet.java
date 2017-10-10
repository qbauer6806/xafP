package mc.gouv.af.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.af.servlet.dto.FileUploadResponseDTO;
import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.af.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.af.servlet.util.AppFactoryServletUtils;
import mc.gouv.af.servlet.util.AppFactoryServletUtils.ServiceTarget;
import mc.gouv.dem.shared.model.AccessDTO;

@MultipartConfig
public class FileUploadServlet extends AbstractAfServlet {

    private static final long serialVersionUID = 484237515919955392L;

    private static Logger LOGGER = LoggerFactory.getLogger(FileUploadServlet.class);

    @SuppressWarnings("deprecation")
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /fileupload doPost()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }

        // Récupération du nom du fichier à envoyer
        String pathInfo = request.getPathInfo();
        String filename = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            filename = pathInfo.split("/")[1];
        }

        if (StringUtils.isBlank(filename)) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                    "Erreur: nom du fichier manquant");
            return;
        }

        try {

            // Génération de l'UUID
            UUID uuid = AppFactoryServletUtils.generateUUID();
            LOGGER.debug("UUID généré : {}", uuid.toString());

            String accountId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);
            String containerId = getServletContext().getInitParameter(AppFactoryServletUtils.CONTAINER_KEY);

            LOGGER.debug("accountId = {}, containerId = {}", accountId, containerId);

            // Récupération de l'AccessID via appel WS à Demarches
            LOGGER.info("Appel à la démarche pour récupérer l'AccessID correspondant..");

            //Integer accessId = AppFactoryServletUtils.getAccessID(demarcheId, usagerInfosDTO.getId());
            AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
            Integer accessId = access.getPkAccess();
            
            LOGGER.debug("AccessID = {}", accessId);

            if (accessId == null) {
                response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_NOT_FOUND,
                        "Erreur: impossible de récupérer l'accès");
                return;
            }

            // Constitution du chemin virtuel du fichier
            // /appfactory/demarcheId/accessId/UUID/nomDuFichier
            String virtualPath = "/" + accountId + "/" + containerId + "/" + accessId + "/" + uuid + "/" + filename;
            LOGGER.info("Chemin virtuel : {}", virtualPath);

            // Constitution de l'URL d'appel
            URL url = new URL(AfServletGouvPropertiesResolver.getFileUrl() + virtualPath);
            LOGGER.info("URL d'appel : {}", url);

            // Extraction du demandeId si le client le connaît déjà et l'a fourni à AFS
            String demandeId = null;
            Enumeration<String> headers = request.getHeaderNames();
            while (headers.hasMoreElements()) {
                String headerName = headers.nextElement();
                if (headerName.startsWith(AppFactoryServletUtils.FILE_METADATA_DEMANDEID)) {
                    demandeId = request.getHeader(headerName);
                }
            }

            // Constitution de la requête
            HttpClient client = HttpClientBuilder.create().build();
            Part part = request.getParts().iterator().next();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("data",
                    new InputStreamBody(part.getInputStream(), part.getContentType(), part.getSubmittedFileName()));
            HttpEntity multipart = builder.build();
            HttpPost postRequest = new HttpPost(url.toString());
            postRequest.setEntity(multipart);
            // Renseigner le demandeId le cas échéant
            if (demandeId != null) {
                postRequest.setHeader(AppFactoryServletUtils.FILE_METADATA_DEMANDEID, demandeId);
            }
            
            postRequest.setHeader(HttpHeaders.AUTHORIZATION, AppFactoryServletUtils.getAuthHeader(ServiceTarget.FILE));

            LOGGER.info("Appel du WS FILE");
            HttpResponse postResponse = client.execute(postRequest);

            // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
            LOGGER.info("Constitution de la réponse pour retour au client");
            response.setContentType("application/json");

            int statusCode = postResponse.getStatusLine().getStatusCode();
            response.setStatus(statusCode);

            if (statusCode == HttpServletResponse.SC_OK || statusCode == HttpServletResponse.SC_CREATED) {
                // Si tout s'est bien passé, alors on forme une réponse différente que celle qui nous est retournée par
                // FILE
                response.setContentType("application/json");
                ObjectMapper mapper = new ObjectMapper();
                // Répondre accessId/uuid/nomDuFichier
                FileUploadResponseDTO responseObj = new FileUploadResponseDTO(accessId + "/" + uuid + "/" + filename);
                String responseStr = mapper.writeValueAsString(responseObj);
                response.getOutputStream().write(responseStr.getBytes());
            } else {
                LOGGER.error("Status code : {}", statusCode);
                // S'il y a eu un problème, alors on retourne le message d'erreur au client
                IOUtils.copy(postResponse.getEntity().getContent(), response.getOutputStream());
            }

        } catch (Exception e) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Erreur interne: ", e);
        }

        LOGGER.info("====================== Fin /fileupload doPost()");
    }

}
