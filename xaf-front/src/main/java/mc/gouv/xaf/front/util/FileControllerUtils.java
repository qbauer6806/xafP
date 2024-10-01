package mc.gouv.xaf.front.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.el.PropertyNotFoundException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mc.gouv.vscan.shared.dto.ScanDTO;
import mc.gouv.vscan.shared.dto.ScanRequestDTO;
import mc.gouv.xaf.front.dto.FileUploadCompteurDTO;
import mc.gouv.xaf.front.dto.FileUploadResponseDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class FileControllerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileControllerUtils.class);
    private static final String EXTENSIONS_WHITELIST = "EXTENSIONS_WHITELIST";
    private static final String MAX_TAILLE_FICHIER = "MAX_TAILLE_FICHIER";
    private static final String SLASH = "/";
    private static final String BEARER = "Bearer ";

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;
    
    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    @Autowired
    private FrontControllerPropertiesCache propertiesCache;

    public boolean estExtensionDansWhitelist(String filename) {
        String[] filenameSplit = filename.split("\\.");

        // Dans le cas où filename = "pdf", on vérifie qu'on a bien un couple nom.ext
        // sinon un fichier nommé "pdf" passerait pour un fichier à l'extension pdf, bien qu'il puisse s'agir d'un .txt par exemple.
        if (filenameSplit.length <= 1) {
            return false;
        }

        String fileExtension = filenameSplit[filenameSplit.length - 1].toLowerCase();
        return getExtensionsWhitelist().contains(fileExtension);
    }

    public List<String> getExtensionsWhitelist() {
        List<String> extensions = new ArrayList<>();
        PropertiesDTO extensionsProperty = propertiesCache.getFrontProperty(EXTENSIONS_WHITELIST);

        if (extensionsProperty != null) {
            String propertyString = extensionsProperty.getValue().replace("*.", "").replace(" ", "");
            String[] types = propertyString.split(",");
            Collections.addAll(extensions, types);
        }

        return extensions;
    }

    public boolean tailleFichierValide(Part part) {
        PropertiesDTO propMaxTailleFichiers = propertiesCache.getFrontProperty(MAX_TAILLE_FICHIER);
        if (propMaxTailleFichiers == null) {
            throw new PropertyNotFoundException("La propriété obligatoire MAX_TAILLE_FICHIER ne semble pas définie");
        }

        long tailleMaxFichier = Long.parseLong(propMaxTailleFichiers.getValue());
        // transformation B en MB
        long tailleMaxFichierMB = tailleMaxFichier * 1_000_000;

        return part.getSize() <= tailleMaxFichierMB;
    }

    public boolean vscan(Part part0, String filename, HttpPost postRequest, ServletContext servletContext) throws IOException {
        // Constitution de la requête
        boolean activationVscan = propertiesResolver.isVscanActivated();

        // Rajouter l'information si le fichier a été scanné par VSCAN ou pas
        postRequest.setHeader(XafFrontserverUtils.FILE_METADATA_SCANEXECUTE, activationVscan + "");
        LOGGER.info("Activation de VSCAN: {}", activationVscan);

        if (activationVscan) {
            LOGGER.info("Appel à VSCAN...");

            String urlVscan = propertiesResolver.getVscanUrl();
            LOGGER.info("URL = {}", urlVscan);
            try (CloseableHttpClient clientVscan = HttpClientBuilder.create().build()) {
                MultipartEntityBuilder builderVscan = MultipartEntityBuilder.create();
                builderVscan.addPart("file", new InputStreamBody(part0.getInputStream(), ContentType.create(part0.getContentType()), part0.getSubmittedFileName()));

                ScanRequestDTO scanRequest = new ScanRequestDTO();
                scanRequest.setCodeAppli(servletContext.getInitParameter(XafFrontserverUtils.DEMARCHEID_KEY));
                scanRequest.setFilename(filename);
                scanRequest.setEnduserAppModule(servletContext.getInitParameter(XafFrontserverUtils.DEMARCHEID_KEY).toLowerCase() + "-frontserver");

                ObjectMapper mapper = new ObjectMapper();
                String scanRequestStr = mapper.writeValueAsString(scanRequest);
                builderVscan.addPart("scanRequest", new StringBody(scanRequestStr, ContentType.TEXT_PLAIN));

                HttpEntity multipartVscan = builderVscan.build();
                HttpPost postRequestVscan = new HttpPost(urlVscan);
                postRequestVscan.setEntity(multipartVscan);
                postRequestVscan.addHeader(HttpHeaders.AUTHORIZATION, BEARER + propertiesResolver.getVscanJwt());

                ClassicHttpResponse postResponseVscan = clientVscan.execute(postRequestVscan);
                String vscanResp = IOUtils.toString(postResponseVscan.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.info("VSCAN Response : {} ({})", postResponseVscan.getCode(), vscanResp);

                ScanDTO scanDto = mapper.readValue(vscanResp, ScanDTO.class);
                if (!scanDto.isResult()) {
                    LOGGER.info("VSCAN a détecté le fichier comme vérolé, fin du traitement, pas d'upload dans FILE");
                    xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST, "Erreur: le fichier soumis semble corrompu");
                    return false;
                }
            }

            LOGGER.info("VSCAN n'a pas considéré le fichier soumis comme vérolé");
        }
        return true;
    }

    /**
     * Vérification du nombre de fichier uploadés sur la demande
     *
     * @return true si la limite a été atteinte, false si il est toujours possible d'uploader
     */
    public synchronized boolean limiteUploadAtteinte(Map<HttpSession, FileUploadCompteurDTO> usagersFileUploadCompteurs, HttpSession session) {
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

    public synchronized void ajouterCompteurUpload(Map<HttpSession, FileUploadCompteurDTO> usagersFileUploadCompteurs, HttpSession session) {
        FileUploadCompteurDTO compteurUpload = usagersFileUploadCompteurs.get(session);
        if (compteurUpload == null) {
            compteurUpload = new FileUploadCompteurDTO();
            compteurUpload.setCompteur(0);
            compteurUpload.setDatePremierUpload(LocalDateTime.now());
        }
        // Ajouter au compteur qu'un nouveau fichier a été uploadé
        compteurUpload.setCompteur(compteurUpload.getCompteur() + 1);
        usagersFileUploadCompteurs.put(session, compteurUpload);
    }

    /**
     * Methode qui parcours toutes les sessions stockées et supprime les entrées qui ne servent plus. ex:
     * Une session dont la date du premier upload > x secondes
     */
    public synchronized void reinitialierSessionsInutilisees(Map<HttpSession, FileUploadCompteurDTO> usagersFileUploadCompteurs) {
        for (Iterator<Map.Entry<HttpSession, FileUploadCompteurDTO>> it = usagersFileUploadCompteurs.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<HttpSession, FileUploadCompteurDTO> entry = it.next();
            LocalDateTime datePremierUpload = entry.getValue().getDatePremierUpload();
            Duration duration = Duration.between(datePremierUpload, LocalDateTime.now());
            int tempsParIntervalle = Integer.parseInt(propertiesResolver.getTempsIntervalleUpload());
            if (duration.toMillis() > tempsParIntervalle) {
                it.remove();
            }
        }
    }

    /**
     * Obtiens un {@link InputStream} d'un fichier distant.
     * Important, le pathInfo doit être au format non encodé et en UTF8 ex :
     * carte d'identité.png (valide)
     * carte+d%27identit%C3%A9.png (invalide)
     *
     * @param pathInfo           nom du fichier à récupérer (Format: /accessId/uuid/filename)
     * @param isPreview          si vrai remplace dans l'entête Content-disposition-header la valeur attachment par inline
     * @param usagerInfoAccessId usagerInfosDTO.getAccessId()
     * @return Un flux correspondant aux données du fichier demandé ou null.
     */
    public ResponseEntity<InputStream> downloadFile(String pathInfo, boolean isPreview, Integer usagerInfoAccessId) {
        try {
            // Récupération du nom du fichier à récupérer (Format: /accessId/uuid/filename)
            String filename = null;
            Integer accessId = null;
            if (pathInfo != null && pathInfo.length() > 1) {
                String[] pathElems = pathInfo.split("/");
                accessId = !pathElems[1].equals("publications") ? Integer.valueOf(pathElems[1]) : null;
                filename = pathElems[1] + "/" + pathElems[2] + "/" + URLEncoder.encode(pathElems[3], StandardCharsets.UTF_8);
            }

            if (StringUtils.isBlank(filename)) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST, "Erreur: nom ou ID du fichier manquant");
            }

            if (accessId != null && (usagerInfoAccessId == null || !usagerInfoAccessId.equals(accessId))) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.FORBIDDEN, "Erreur: accès à ce fichier non autorisé");
            }

            String accountId = propertiesResolver.getDemarcheId().toUpperCase();
            String containerId = XafFrontserverUtils.CONTAINER_ROOT;

            LOGGER.debug("accountId = {}, containerId = {}", accountId, containerId);

            // Constitution du chemin virtuel du fichier
            // /appfactory/demarcheId/accessId/UUID/nomDuFichier
            String virtualPath = "/" + accountId + "/" + containerId + "/" + filename;
            LOGGER.info("Chemin virtuel : {}", virtualPath);

            // Constitution de l'URL d'appel
            URL url = new URL(propertiesResolver.getFileUrl() + virtualPath);
            LOGGER.info("URL d'appel : {}", url);

            // Constitution de la requête
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(url.toString());

            getRequest.setHeader(HttpHeaders.AUTHORIZATION, xafFrontserverUtils.getAuthHeader(XafFrontserverUtils.ServiceTarget.FILE));

            LOGGER.info("Appel du WS FILE");
            ClassicHttpResponse getResponse = (ClassicHttpResponse) client.execute(getRequest);

            LOGGER.info("Constitution de la réponse pour retour au client");
            ResponseEntity.BodyBuilder response = ResponseEntity.status(getResponse.getCode())
                            .contentType(MediaType.valueOf(getResponse.getEntity().getContentType()));
            // Ajout de la métadonnée indiquant le demandeId lié
            for (Header header : getResponse.getHeaders()) {
                if (header.getName().startsWith(XafFrontserverUtils.FILE_METADATA_DEMANDEID)) {
                    response.header(header.getName(), header.getValue());
                } else if (header.getName().equals(RequestConstant.CONTENT_DISPOSITION_HEADER)) {
                    String headerValue = isPreview ? header.getValue().replace("attachment;", "inline;") : header.getValue();
                    response.header(header.getName(), URLDecoder.decode(headerValue, StandardCharsets.UTF_8));
                }
            }

            return response.body(getResponse.getEntity().getContent());
        } catch (IOException | NumberFormatException e) {
            LOGGER.error("FileServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * @param docHolderUrl l'adresse à laquelle envoyer la requête
     * @param pathInfo     le nom du fichier à télécharger dans le portedocument ex : d738aa26-588a-11ee-a76d-005056bfb0c9/docholderwishlist.png
     * @param accessToken  le token d'accès à l'API, du compte connecté
     */
    public HttpResponse downloadFromDocHolder(String docHolderUrl, String pathInfo, String accessToken) throws IOException {
        MultipartEntityBuilder multipart = MultipartEntityBuilder.create().addPart("filename", new StringBody(pathInfo, ContentType.MULTIPART_FORM_DATA.withCharset("UTF-8")));

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(URI.create(docHolderUrl));
        request.addHeader(HttpHeaders.AUTHORIZATION, BEARER + accessToken);
        request.setEntity(multipart.build());

        return client.execute(request);
    }

    public HttpResponse uploadToDocHolder(String docHolderUrl, InputStream filestream, String accessToken, String filename, String typedoc, String preferredName, String endOfValidity) throws IOException {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .setCharset(StandardCharsets.UTF_8)
                .addPart("file", new InputStreamBody(filestream, filename))
                .addPart("preferredName", new StringBody(preferredName, ContentType.MULTIPART_FORM_DATA.withCharset("UTF-8")))
                .addTextBody("typedoc", typedoc)
                .setMode(HttpMultipartMode.EXTENDED);

        if (!StringUtils.isEmpty(endOfValidity)) {
            entityBuilder.addTextBody("endOfValidity", endOfValidity);
        }

        Request serviceRequest = Request.post(docHolderUrl);
        serviceRequest.body(entityBuilder.build());
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, BEARER + accessToken);

        LOGGER.info("Envoi de la requête");
        return serviceRequest.execute().returnResponse();
    }

    /**
     * Upload un fichier dans FILE.
     * <b>/!\ Attention : aucune validation vscan/taille n'est faite dans la méthode !</b>
     *
     * @param filename       le nom du fichier à envoyer
     * @param typeModele     le type de modèle de document, valeur de l'en-tête X-MC-TypeModele
     * @param filestream     le flux du fichier à envoyer
     */
    public ResponseEntity uploadToFILE(UsagerInfosDTO usagerInfosDTO, String filename, String typeModele, InputStream filestream) throws URISyntaxException, IOException {
        // Génération de l'UUID
        UUID uuid = XafFrontserverUtils.generateUUID();
        LOGGER.debug("UUID généré : {}", uuid);

        // Récupération de l'AccessID via appel WS à Demarches
        LOGGER.info("Appel à la démarche pour récupérer l'AccessID correspondant..");
        AccessDTO access = xafFrontserverUtils.getAfApiClient().getAccess(usagerInfosDTO.getId());
        Integer accessId = access.getPkAccess();
        LOGGER.debug("AccessID = {}", accessId);
        if (accessId == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.NOT_FOUND, "Erreur : impossible de récupérer l'accès");
        }

        URI uri = generateFileUrl(uuid, accessId, filename);
        HttpPost postRequest = new HttpPost(uri);

        // Extraction du demandeId si le client le connaît déjà et l'a fourni à AFS
        //extraireDemandeId(postRequest, request);
        postRequest.setHeader(XafFrontserverUtils.FILE_METADATA_TYPEMODELE, typeModele);

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("data", new InputStreamBody(filestream, filename));
        HttpEntity multipart = builder.build();
        postRequest.setEntity(multipart);
        postRequest.setHeader(HttpHeaders.AUTHORIZATION, xafFrontserverUtils.getAuthHeader(XafFrontserverUtils.ServiceTarget.FILE));

        LOGGER.info("Appel du WS FILE");
        HttpResponse postResponse = client.execute(postRequest);

        // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
        LOGGER.info("Constitution de la réponse pour retour au client");
        return constituerReponse(filename, uuid, accessId, postResponse);
    }

    /**
     * Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
     */
    public ResponseEntity constituerReponse(String filename, UUID uuid, Integer accessId, HttpResponse postResponse) throws IOException {
        int statusCode = postResponse.getCode();
        ResponseEntity.BodyBuilder response = ResponseEntity.status(statusCode);
        if (statusCode == HttpServletResponse.SC_OK || statusCode == HttpServletResponse.SC_CREATED) {
            // Si tout s'est bien passé, alors on forme une réponse différente que celle qui nous est retournée par FILE
            // Répondre /accessId/uuid/nomDuFichier
            FileUploadResponseDTO responseObj = new FileUploadResponseDTO(SLASH + accessId + SLASH + uuid + SLASH + filename);
            return response.body(responseObj);
        } else {
            LOGGER.error("Status code : {}", statusCode);
            // S'il y a eu un problème, alors on retourne le message d'erreur au client
            return response.body(new String(((ClassicHttpResponse)postResponse).getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Renseigne le demandeId dans la requête de création du fichier s'il est déjà connu
     */
    public void extraireDemandeId(HttpPost postRequest, HttpServletRequest request) {
        String demandeId = null;
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            if (headerName.startsWith(XafFrontserverUtils.FILE_METADATA_DEMANDEID)) {
                demandeId = request.getHeader(headerName);
            }
        }
        if (demandeId != null) {
            postRequest.setHeader(XafFrontserverUtils.FILE_METADATA_DEMANDEID, demandeId);
        }
    }

    /**
     * Permet de parser le nom du fichier depuis le Path Info de la requête
     *
     * @param pathInfo ex : /2/8a9b43f1-5de1-11ee-b33e-9efce89c72aa/docholderwishlist.png
     * @return null ou le nom du fichier (docholderwishlist.png selon l'exemple)
     */
    public String getFilename(String pathInfo) {
        String filename = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            String[] splitPath = pathInfo.split(SLASH);
            filename = splitPath[splitPath.length - 1];
        }
        return filename;
    }

    public URI generateFileUrl(UUID uuid, Integer accessId, String filename) throws URISyntaxException {
        String accountId = propertiesResolver.getDemarcheId().toUpperCase();
        String containerId = XafFrontserverUtils.CONTAINER_ROOT;

        LOGGER.debug("accountId = {}, containerId = {}", accountId, containerId);

        // Constitution du chemin virtuel du fichier
        // /appfactory/demarcheId/accessId/UUID/nomDuFichier
        String virtualPath = SLASH + accountId + SLASH + containerId + SLASH + accessId + SLASH + uuid + SLASH + URLEncoder.encode(filename, StandardCharsets.UTF_8);
        LOGGER.info("Chemin virtuel : {}", virtualPath);

        // Constitution de l'URL d'appel
        return new URI(propertiesResolver.getFileUrl() + virtualPath);
    }
}
