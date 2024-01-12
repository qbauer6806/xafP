package mc.gouv.candifp.frontserver.movetoxaf.controller;

import mc.gouv.candifp.frontserver.movetoxaf.dto.UsagerInfosDTO;
import mc.gouv.candifp.frontserver.movetoxaf.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Servlet servant à télécharger ou visualiser un fichier de FILE.
 *
 * @author qdeme
 */
@Controller
public class FileController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    public ResponseEntity doGet(String accessId, String uuid, String filename, HttpServletRequest request, boolean isPreview) throws IOException {
        LOGGER.info("====================== /fileservlet doGet()");

        try {
            UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
            if (usagerInfosDTO == null) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                        SharedMessages.UTILISATEUR_NON_AUTORISE);
            }

            // TODO pourquoi publications??????
            // Récupération du nom du fichier à récupérer (Format: /accessId/uuid/filename)
//            String pathInfo = request.getPathInfo();
//            Integer accessId = null;
//            if (pathInfo != null && pathInfo.length() > 1) {
//                String[] pathElems = pathInfo.split("/");
//                accessId = !pathElems[1].equals("publications") ? Integer.valueOf(pathElems[1]) : null;
//                filename = pathElems[1] + "/" + pathElems[2] + "/" + URLEncoder.encode(pathElems[3], "UTF-8");
//            }

            if (StringUtils.isBlank(filename)) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                        "Erreur: nom ou ID du fichier manquant");
            }

            if (accessId != null && (usagerInfosDTO.getAccessId() == null || !usagerInfosDTO.getAccessId().equals(Integer.parseInt(accessId)))) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.FORBIDDEN,
                        "Erreur: accès à ce fichier non autorisé");
            }

            String accountId = propertiesResolver.getDemarcheId().toUpperCase();
            String containerId = xafFrontserverUtils.CONTAINER_ROOT;

            LOGGER.debug("accountId = {}, containerId = {}", accountId, containerId);

            // Constitution du chemin virtuel du fichier
            // /appfactory/demarcheId/accessId/UUID/nomDuFichier
            String fullFilename=accessId + "/" + uuid + "/" + URLEncoder.encode(filename, "UTF-8");
            String virtualPath = "/" + accountId + "/" + containerId + "/" + fullFilename;
            LOGGER.info("Chemin virtuel : {}", virtualPath);

            // Constitution de l'URL d'appel
            URL url = new URL(propertiesResolver.getFileUrl() + virtualPath);
            LOGGER.info("URL d'appel : {}", url);

            // Constitution de la requête
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(url.toString());

            getRequest.setHeader(HttpHeaders.AUTHORIZATION, xafFrontserverUtils.getAuthHeader(XafFrontserverUtils.ServiceTarget.FILE));

            LOGGER.info("Appel du WS FILE");
            HttpResponse getResponse = client.execute(getRequest);

            LOGGER.info("Constitution de la réponse pour retour au client");
            ResponseEntity.BodyBuilder response = ResponseEntity.status(getResponse.getStatusLine().getStatusCode())
                    .contentType(MediaType.valueOf(getResponse.getEntity().getContentType().getValue()));
            // Ajout de la métadonnée indiquant le demandeId lié
            for (Header header : getResponse.getAllHeaders()) {
                if (header.getName().startsWith(xafFrontserverUtils.FILE_METADATA_DEMANDEID)) {
                    response.header(header.getName(), header.getValue());
                } else if (header.getName().equals(RequestConstant.CONTENT_DISPOSITION_HEADER)) {
                    String headerValue = isPreview ? header.getValue().replace("attachment;", "inline;") : header.getValue();
                    response.header(header.getName(), URLDecoder.decode(headerValue, "UTF-8"));
                }
            }

            LOGGER.info("====================== Fin /fileservlet doGet()");

            return response.body(new InputStreamResource(getResponse.getEntity().getContent()));
        } catch (IOException | NumberFormatException e) {
            LOGGER.error("FileServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
