package mc.gouv.xaf.front.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.front.dto.FileUploadCompteurDTO;
import mc.gouv.xaf.front.dto.FileUploadResponseDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.FrontControllerPropertiesCache;
import mc.gouv.vscan.shared.dto.ScanDTO;
import mc.gouv.vscan.shared.dto.ScanRequestDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servlet servant à uploader un fichier dans FILE.
 *
 * @author qdeme
 */
@Controller
@MultipartConfig
public class FileUploadController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);

    private static final String EXTENSIONS_WHITELIST = "EXTENSIONS_WHITELIST";
    private static final String MAX_TAILLE_FICHIER = "MAX_TAILLE_FICHIER";
    private static final String VSCAN_ACTIVATION = "VSCAN_ACTIVATION";
    private static final String SLASH = "/";

    // Enregistre l'historique d'upload par session
    private static final Map<HttpSession, FileUploadCompteurDTO> usagersFileUploadCompteurs = new HashMap<>();

    // Compteur permettant de trigger un refresh des sessions et supprimer celles qui ne sont plus utilisées
    private static int compteurCleanSessions;

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    @Autowired
    private FrontControllerPropertiesCache propertiesCache;

    @PostMapping(value = {"/fileupload", "/fileupload/{filename}"})
    public ResponseEntity<FileUploadResponseDTO> doPost(@PathVariable(required = false) String filename,
                                 HttpServletRequest request) {
        LOGGER.info("====================== /fileupload doPost()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        // Vérification du nombre de fichier uploadés sur la demande
        LOGGER.info("Vérification du nombre de fichiers déjà uploadés...");
        HttpSession session = request.getSession();
        if (verifierNombreFichiers(session)) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    "La limite de nombre de fichiers uploadés a été atteinte");
        }

        // Récupération du nom du fichier à envoyer
        if (StringUtils.isBlank(filename)) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    "Erreur: nom du fichier manquant");
        }

        // ---  Vérification de la conformité du fichier
        // Vérification du type du fichier
        LOGGER.info("Vérification du type pour le fichier {} ...", filename);
        if (!estExtensionDansWhitelist(filename)) {
            LOGGER.info("Le type de fichier ne correspond pas aux types whitelistés ({}), pas d'upload dans FILE", getExtensionsWhitelist());
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.FORBIDDEN,
                    "Erreur: le type/extension du fichier soumis n'est pas valide");
        }

        try {
            HttpPost postRequest = new HttpPost();
            Part part = request.getParts().iterator().next();

            LOGGER.info("Vérification de la taille...");
            // Vérification de la taille du fichier
            PropertiesDTO propMaxTailleFichiers = propertiesCache.getFrontProperty(MAX_TAILLE_FICHIER);
            if (propMaxTailleFichiers == null) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                        "La propriété obligatoire MAX_TAILLE_FICHIER ne semble pas définie");
            }
            int tailleMaxFichier = Integer.parseInt(propMaxTailleFichiers.getValue());
            // transformation B en MB
            int tailleMaxFichierMB = tailleMaxFichier * 1000000;
            if (part.getSize() > tailleMaxFichierMB) {
                LOGGER.info("La taille du fichier depasse la taille max definie dans les propriétés ({})", tailleMaxFichier);
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.FORBIDDEN,
                        "Erreur: la taille du fichier depasse la taille max definie dans les propriétés");
            }

            // Appel à VSCAN afin d'effectuer le scan antivirus
            if (!vscan(part, filename, postRequest)) {
                return ResponseEntity.badRequest().build();
            }

            // Génération de l'UUID
            UUID uuid = xafFrontserverUtils.generateUUID();
            LOGGER.debug("UUID généré : {}", uuid);

            String accountId = propertiesResolver.getDemarcheId().toUpperCase();
            String containerId = xafFrontserverUtils.CONTAINER_ROOT;

            LOGGER.debug("accountId = {}, containerId = {}", accountId, containerId);

            // Récupération de l'AccessID via appel WS à Demarches
            LOGGER.info("Appel à la démarche pour récupérer l'AccessID correspondant..");
            AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
            Integer accessId = access.getPkAccess();
            LOGGER.debug("AccessID = {}", accessId);
            if (accessId == null) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.NOT_FOUND,
                        "Erreur: impossible de récupérer l'accès");
            }

            // Constitution du chemin virtuel du fichier
            // /appfactory/demarcheId/accessId/UUID/nomDuFichier
            String virtualPath = SLASH + accountId + SLASH + containerId + SLASH + accessId + SLASH + uuid + SLASH + URLEncoder.encode(filename, StandardCharsets.UTF_8);
            LOGGER.info("Chemin virtuel : {}", virtualPath);

            // Constitution de l'URL d'appel
            URI url = new URI(propertiesResolver.getFileUrl() + virtualPath);
            postRequest.setURI(url);
            LOGGER.info("URL d'appel : {}", url);

            // Extraction du demandeId si le client le connaît déjà et l'a fourni à AFS
            extraireDemandeId(postRequest, request);

            // Constitution de la requête
            HttpClient client = HttpClientBuilder.create().build();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("data",
                    new InputStreamBody(part.getInputStream(), ContentType.create(part.getContentType()), part.getSubmittedFileName()));
            HttpEntity multipart = builder.build();
            postRequest.setEntity(multipart);
            postRequest.setHeader(HttpHeaders.AUTHORIZATION, xafFrontserverUtils.getAuthHeader(XafFrontserverUtils.ServiceTarget.FILE));

            LOGGER.info("Appel du WS FILE");
            HttpResponse postResponse = client.execute(postRequest);

            // Supression des sessions inutilisées chaque 10 requêtes d'upload
            if (compteurCleanSessions > 50) {
                reinitialierSessionsInutilisees();
            }

            // Ajout dans l'historique par session
            ajouterCompteurUpload(session);

            LOGGER.info("====================== Fin /fileupload doPost()");

            // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
            LOGGER.info("Constitution de la réponse pour retour au client");
            return constituerReponse(filename, uuid, accessId, postResponse);

        } catch (Exception e) {
            LOGGER.error("FileUploadServlet - Une erreur est survenue lors de l'appel à la méthode POST", e);
            return ResponseEntity.status(getCodeErreur(e)).build();
        }

    }

    /**
     * Vérification du nombre de fichier uploadés sur la demande
     */
    private boolean verifierNombreFichiers(HttpSession session) {
        FileUploadCompteurDTO compteurUpload = usagersFileUploadCompteurs.get(session);
        if (compteurUpload != null) {
            Duration duration = Duration.between(compteurUpload.getDatePremierUpload(), LocalDateTime.now());
            int tempsParIntervalle = Integer.parseInt(propertiesResolver.getTempsIntervalleUpload());
            int maxUploadParIntervalle = Integer.parseInt(propertiesResolver.getMaxUploadParIntervalle());

            if (compteurUpload.getCompteur() >= maxUploadParIntervalle && duration.toMillis() < tempsParIntervalle) {
                return true;
            } else if (duration.toMillis() > tempsParIntervalle) {
                // Supprimer le compteur en cas de dépassement
                usagersFileUploadCompteurs.remove(session);
            }
        }
        return false;
    }

    /**
     * Permet de parser le nom du fichier depuis le Path Info de la requête
     */
    private String getFilename(String pathInfo) {
        String filename = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            filename = pathInfo.split(SLASH)[1];
        }
        return filename;
    }

    /**
     * Méthode permettant d'appeler VSCAN afin d'effectuer le scan antivirus.
     */
    private boolean vscan(Part part0, String filename, HttpPost postRequest) throws IOException {
        // Varification de l'activation de VSCAN
        PropertiesDTO propActivationVscan = propertiesCache.getFrontProperty(VSCAN_ACTIVATION);
        if (propActivationVscan == null) {
            return false;
        }

        // Constitution de la requête
        boolean activationVscan = Boolean.parseBoolean(propActivationVscan.getValue());
        // Rajouter l'information si le fichier a été scanné par VSCAN ou pas
        postRequest.setHeader(xafFrontserverUtils.FILE_METADATA_SCANEXECUTE, activationVscan + "");
        LOGGER.info("Activation de VSCAN: {}", activationVscan);

        if (activationVscan) {
            LOGGER.info("Appel à VSCAN...");

            String urlVscan = propertiesResolver.getVscanUrl();
            LOGGER.info("URL = {}", urlVscan);
            HttpClient clientVscan = HttpClientBuilder.create().build();
            MultipartEntityBuilder builderVscan = MultipartEntityBuilder.create();
            builderVscan.addPart("file", new InputStreamBody(part0.getInputStream(),
                    ContentType.create(part0.getContentType()), part0.getSubmittedFileName()));

            ScanRequestDTO scanRequest = new ScanRequestDTO();
            scanRequest.setCodeAppli(propertiesResolver.getDemarcheId());
            scanRequest.setFilename(filename);
            scanRequest.setEnduserAppModule(propertiesResolver.getDemarcheId().toLowerCase() + "-frontserver");

            ObjectMapper mapper = new ObjectMapper();
            String scanRequestStr = mapper.writeValueAsString(scanRequest);
            builderVscan.addPart("scanRequest", new StringBody(scanRequestStr, ContentType.TEXT_PLAIN));
            HttpEntity multipartVscan = builderVscan.build();
            HttpPost postRequestVscan = new HttpPost(urlVscan);
            postRequestVscan.setEntity(multipartVscan);
            postRequestVscan.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + propertiesResolver.getVscanJwt());
            HttpResponse postResponseVscan = clientVscan.execute(postRequestVscan);
            String vscanResp = IOUtils.toString(postResponseVscan.getEntity().getContent());
            LOGGER.info("VSCAN Response : {} ({})", postResponseVscan.getStatusLine(), vscanResp);

            ScanDTO scanDto = mapper.readValue(vscanResp, ScanDTO.class);
            if (!scanDto.isResult()) {
                LOGGER.info("VSCAN a détecté le fichier comme vérolé, fin du traitement, pas d'upload dans FILE");
                return false;
            }

            LOGGER.info("VSCAN n'a pas considéré le fichier soumis comme vérolé");
        }
        return true;
    }

    /**
     * Renseigne le demandeId dans la requête de création du fichier s'il est déjà connu
     */
    private void extraireDemandeId(HttpPost postRequest, HttpServletRequest request) {
        String demandeId = null;
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            if (headerName.startsWith(xafFrontserverUtils.FILE_METADATA_DEMANDEID)) {
                demandeId = request.getHeader(headerName);
            }
        }
        if (demandeId != null) {
            postRequest.setHeader(xafFrontserverUtils.FILE_METADATA_DEMANDEID, demandeId);
        }
    }

    /**
     * Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
     */
    private ResponseEntity<FileUploadResponseDTO> constituerReponse(String filename, UUID uuid, Integer accessId, HttpResponse postResponse) throws IOException {
        int statusCode = postResponse.getStatusLine().getStatusCode();
        ResponseEntity response;
        if (statusCode == HttpServletResponse.SC_OK || statusCode == HttpServletResponse.SC_CREATED) {
            // Si tout s'est bien passé, alors on forme une réponse différente que celle qui nous est retournée par FILE
            FileUploadResponseDTO responseObj = new FileUploadResponseDTO(SLASH + accessId + SLASH + uuid + SLASH + filename);
            response = ResponseEntity.status(statusCode).body(responseObj);
        } else {
            LOGGER.error("Status code : {}", statusCode);
            // S'il y a eu un problème, alors on retourne le message d'erreur au client
            response = ResponseEntity.status(statusCode).body(postResponse.getEntity().getContent());
        }

        return response;
    }

    private boolean estExtensionDansWhitelist(String filename) {
        String[] filenameSplit = filename.split("\\.");
        String fileExtension = filenameSplit[filenameSplit.length - 1].toLowerCase();
        return getExtensionsWhitelist().contains(fileExtension);
    }

    private List<String> getExtensionsWhitelist() {
        List<String> extensions = new ArrayList<>();
        PropertiesDTO extensionsProperty = propertiesCache.getFrontProperty(EXTENSIONS_WHITELIST);

        if (extensionsProperty != null) {
            String propertyString = extensionsProperty.getValue().replace("*.", "").replace(" ", "");
            String[] types = propertyString.split(",");
            Collections.addAll(extensions, types);
        }

        return extensions;
    }

    private static synchronized void ajouterCompteurUpload(HttpSession session) {
        FileUploadCompteurDTO compteurUpload = usagersFileUploadCompteurs.get(session);
        if (compteurUpload == null) {
            compteurUpload = new FileUploadCompteurDTO();
            compteurUpload.setCompteur(0);
            compteurUpload.setDatePremierUpload(LocalDateTime.now());
        }
        // Ajouter au compteur qu'un nouveau fichier a été uploadé
        compteurUpload.setCompteur(compteurUpload.getCompteur() + 1);
        usagersFileUploadCompteurs.put(session, compteurUpload);
        compteurCleanSessions++;
    }

    /**
     * Methode qui parcours toutes les sessions stockées et supprime les entrées qui ne servent plus. ex:
     * Une session dont la date du premier upload > x secondes
     */
    private synchronized void reinitialierSessionsInutilisees() {
        for (Iterator<Map.Entry<HttpSession, FileUploadCompteurDTO>> it = usagersFileUploadCompteurs.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<HttpSession, FileUploadCompteurDTO> entry = it.next();
            LocalDateTime datePremierUpload = entry.getValue().getDatePremierUpload();
            Duration duration = Duration.between(datePremierUpload, LocalDateTime.now());
            int tempsParIntervalle = Integer.parseInt(propertiesResolver.getTempsIntervalleUpload());
            if (duration.toMillis() > tempsParIntervalle) {
                it.remove();
            }
        }
        compteurCleanSessions = 0;
    }
}
