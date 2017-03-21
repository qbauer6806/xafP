package mc.gouv.af.servlet;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
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

import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.af.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.af.servlet.util.AppFactoryServletUtils;
import mc.gouv.af.servlet.util.AppFactoryServletUtils.ServiceTarget;

public class FileDownloadServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -2464829773835748491L;

    private static Logger LOGGER = LoggerFactory.getLogger(FileDownloadServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /filedownload doGet()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }

        // Récupération du nom du fichier à récupérer
        String pathInfo = request.getPathInfo();
        String filename = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            // Format: /accessId/uuid/filename
            filename = pathInfo.split("/")[1] + "/" + pathInfo.split("/")[2] + "/" + pathInfo.split("/")[3];
        }

        if (StringUtils.isBlank(filename)) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                    "Erreur: nom du fichier manquant");
            return;
        }

        try {

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

            LOGGER.info("Appel du WS FILE");
            HttpResponse getResponse = client.execute(getRequest,
                    AppFactoryServletUtils.getHttpContextForAuth(url, ServiceTarget.FILE));

            LOGGER.info("Constitution de la réponse pour retour au client");
            response.setStatus(getResponse.getStatusLine().getStatusCode());
            response.setContentType(getResponse.getEntity().getContentType().getValue());
            // Ajout de la métadonnée indiquant le demandeId lié
            for (Header header : getResponse.getAllHeaders()) {
                if (header.getName().startsWith(AppFactoryServletUtils.FILE_METADATA_DEMANDEID)) {
                    response.addHeader(header.getName(), header.getValue());
                } else if (header.getName().equals("Content-Disposition")) {
                    response.addHeader(header.getName(), header.getValue());
                }
            }

            // Et en dernier on copie le stream... Car si on met les headers après, ils sont tous ignorés !
            IOUtils.copy(getResponse.getEntity().getContent(), response.getOutputStream());

        } catch (Exception e) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Erreur interne: ", e);
        }

        LOGGER.info("====================== Fin /filedownload doGet()");

    }

}
