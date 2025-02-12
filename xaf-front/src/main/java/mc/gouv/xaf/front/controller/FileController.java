package mc.gouv.xaf.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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

    private static final String SLASH = "/";

    @GetMapping(value = { "/file/{accessId}/{uuid}/{filename}" }, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity doGet(@PathVariable(required = false) String accessId,
            @PathVariable(required = false) String uuid, @PathVariable(required = false) String filename,
            @RequestParam(value = "mode", required = false) String mode, HttpServletRequest request)
            throws IOException {
        LOGGER.info("====================== /filedownload doGet()");

        try {
            LOGGER.info("====================== /fileservlet doGet()");
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

            if (accessId != null && (usagerInfosDTO.getAccessId() == null || !usagerInfosDTO.getAccessId()
                    .equals(Integer.parseInt(accessId)))) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.FORBIDDEN,
                        "Erreur: accès à ce fichier non autorisé");
            }

            String accountId = propertiesResolver.getDemarcheId().toUpperCase();
            String containerId = XafFrontserverUtils.CONTAINER_ROOT;

            LOGGER.debug("accountId = {}, containerId = {}", accountId, containerId);

            // Constitution du chemin virtuel du fichier
            // /appfactory/demarcheId/accessId/UUID/nomDuFichier
            String fullFilename = accessId + SLASH + uuid + SLASH + URLEncoder.encode(filename, StandardCharsets.UTF_8);
            String virtualPath = SLASH + accountId + SLASH + containerId + SLASH + fullFilename;
            LOGGER.info("Chemin virtuel : {}", virtualPath);

            // Constitution de l'URL d'appel
            URL url = new URL(propertiesResolver.getFileUrl() + virtualPath);
            LOGGER.info("URL d'appel : {}", url);

            // Constitution de la requête
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(url.toString());

            getRequest.setHeader(HttpHeaders.AUTHORIZATION,
                    xafFrontserverUtils.getAuthHeader(XafFrontserverUtils.ServiceTarget.FILE));

            LOGGER.info("Appel du WS FILE");
            ClassicHttpResponse getResponse = (ClassicHttpResponse) client.execute(getRequest);
            String contentType = getResponse.getEntity().getContentType();

            LOGGER.info("Constitution de la réponse pour retour au client");
            ResponseEntity.BodyBuilder response = ResponseEntity.status(getResponse.getCode())
                    .header(HttpHeaders.CONTENT_TYPE, contentType);
            // Ajout de la métadonnée indiquant le demandeId lié
            for (Header header : getResponse.getHeaders()) {
                if (header.getName().startsWith(XafFrontserverUtils.FILE_METADATA_DEMANDEID)) {
                    response.header(header.getName(), header.getValue());
                } else if (header.getName().equals(RequestConstant.CONTENT_DISPOSITION_HEADER)) {
                    String headerValue = "preview".equalsIgnoreCase(mode) ? header.getValue()
                            .replace("attachment;", "inline;") : header.getValue();
                    response.header(header.getName(), URLDecoder.decode(headerValue, StandardCharsets.UTF_8));
                }
            }

            LOGGER.info("====================== Fin /fileservlet doGet()");

            return response.body(new InputStreamResource(getResponse.getEntity().getContent()));

        } catch (Exception e) {
            LOGGER.error("FileController - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping(value = { "/file/{accessId}/{uuid}/{filename}" })
    public ResponseEntity doDelete(@PathVariable(required = false) String accessId,
            @PathVariable(required = false) String uuid, @PathVariable(required = false) String filename,
            HttpServletRequest request) {
        LOGGER.info("====================== /file doDelete()");
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }
        if (StringUtils.isBlank(filename)) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    "Erreur: nom ou ID du fichier manquant");
        }
        if (accessId != null && (usagerInfosDTO.getAccessId() == null || !usagerInfosDTO.getAccessId()
                .equals(Integer.parseInt(accessId)))) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.FORBIDDEN,
                    "Erreur: accès à ce fichier non autorisé");
        }
        AfApiClient afApiClient = getAfApiClient();

        LOGGER.info("Appel à la démarche pour supprimer le brouillon");
        try {
            afApiClient.deleteFile(accessId + "/" + uuid + "/" + filename);
            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            LOGGER.error("BrouillonsServlet - Une erreur est survenue lors de l'appel à la méthode DELETE", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
