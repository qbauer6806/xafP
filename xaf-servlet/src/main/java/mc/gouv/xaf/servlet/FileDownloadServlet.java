package mc.gouv.xaf.servlet;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mc.gouv.xaf.servlet.enums.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils.ServiceTarget;

/**
 * 
 * Servlet servant à télécharger un fichier de FILE.
 * 
 * @author qdeme
 *
 */
public class FileDownloadServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -2464829773835748491L;

    private static Logger LOGGER = LoggerFactory.getLogger(FileDownloadServlet.class);

    public HttpServletResponse doHttpMethod(HttpServletRequest request, HttpServletResponse response,
                                            HttpMethod httpMethod) throws IOException {

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
        }

        // Récupération du nom du fichier à récupérer
        String pathInfo = request.getPathInfo();
        String filename = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            // Format: /accessId/uuid/filename
            filename = pathInfo.split("/")[1] + "/" + pathInfo.split("/")[2] + "/" + URLEncoder.encode(pathInfo.split("/")[3], "UTF-8");
        }

        if (StringUtils.isBlank(filename)) {
            return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                    "Erreur: nom du fichier manquant");
        }

        String accessIdStr = pathInfo.split("/")[1];
        if (usagerInfosDTO.getAccessId() == null || !usagerInfosDTO.getAccessId().equals(Integer.parseInt(accessIdStr))) {
            return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_FORBIDDEN,
                    "Erreur: accès à ce fichier non autorisé");
        }

        String accountId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);
        String containerId = getServletContext().getInitParameter(AppFactoryServletUtils.CONTAINER_KEY);

        LOGGER.debug("accountId = {}, containerId = {}", accountId, containerId);

        // Constitution du chemin virtuel du fichier
        // /appfactory/demarcheId/accessId/UUID/nomDuFichier
        String virtualPath = "/" + accountId + "/" + containerId + "/" + filename;
        LOGGER.info("Chemin virtuel : {}", virtualPath);

        // Constitution de l'URL d'appel
        URL url = new URL(AfServletGouvPropertiesResolver.getFileUrl() + virtualPath);
        LOGGER.info("URL d'appel : {}", url);

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(url.toString());

        getRequest.setHeader(HttpHeaders.AUTHORIZATION, AppFactoryServletUtils.getAuthHeader(ServiceTarget.FILE));

        LOGGER.info("Appel du WS FILE");
        HttpResponse getResponse = client.execute(getRequest);

        LOGGER.info("Constitution de la réponse pour retour au client");
        response.setStatus(getResponse.getStatusLine().getStatusCode());
        response.setContentType(getResponse.getEntity().getContentType().getValue());
        // Ajout de la métadonnée indiquant le demandeId lié
        for (Header header : getResponse.getAllHeaders()) {
            if (header.getName().startsWith(AppFactoryServletUtils.FILE_METADATA_DEMANDEID)) {
                response.addHeader(header.getName(), header.getValue());
            } else if (header.getName().equals("Content-Disposition")) {
                response.addHeader(header.getName(), URLDecoder.decode(header.getValue(), "UTF-8"));
            }
        }

        // Et en dernier on copie le stream... Car si on met les headers après, ils sont tous ignorés !
        IOUtils.copy(getResponse.getEntity().getContent(), response.getOutputStream());

        return response;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /filedownload doGet()");

        try {
            doHttpMethod(request, response, HttpMethod.GET);
        } catch (Exception e) {
            LOGGER.error("FileDownloadServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /filedownload doGet()");

    }

}
