package mc.gouv.xaf.back.service.itg.file.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.el.PropertyNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import mc.gouv.xaf.back.data.dao.BrouillonsFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesComplementsFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesCourriersRepository;
import mc.gouv.xaf.back.data.dao.DemandesFilesRepository;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.exception.FileUploadException;
import mc.gouv.xaf.back.exception.VScanException;
import mc.gouv.xaf.back.exception.enums.FileUploadErrorEnum;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.file.service.dto.FileBatchDTO;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.vscan.ScanDTO;
import mc.gouv.xaf.shared.dto.vscan.ScanRequestDTO;
import mc.gouv.xaf.shared.util.FileNameUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service d'appel à FILE pour les démarches
 *
 * @author qdeme
 */
@Component
public class FileServiceImpl implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);

    private static final String AUTHORIZATION_PREFIX = "Bearer ";
    private static final String FILENAME_DONNER_FILE_LOG_MESSAGE = "Filename à donner à FILE : {}";
    private static final String FILECLIENT_SAVE_FILE_LOG_MESSAGE = "FileClient.saveFile({}, {}, {})";
    private static final String SLASH_DELIMITER = "/";
    private static final String ERREUR_FILESERVICE_LOG_MESSAGE = "Erreur dans FileServiceImpl.saveFile()";
    private static final String MESSAGE_FICHIER_REFERENCE = "Le fichier {} n'a pas été supprimé car il est référencé autre part";
    private static final String DEBUT_FILE_SERVICE_GET_FILE = "Début FileService.getFile({}, {})";
    private static final String FIN_FILE_CLIENT_GET_FILE = "Fin FileClient.getFile({}, {}, {})";

    private RestTemplate restTemplate;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesFilesRepository demandesFilesRepository;

    @Autowired
    private DemandesCourriersRepository demandesCourriersRepository;

    @Autowired
    private BrouillonsFilesRepository brouillonsFilesRepository;

    @Autowired
    private DemandesComplementsFilesRepository demandesComplementsFilesRepository;

    @Override
    public void getFile(String filename, String containerId, HttpServletResponse response) throws IOException {
        LOGGER.info(DEBUT_FILE_SERVICE_GET_FILE, filename, containerId);
        String accountId = gouvPropertiesResolver.getDemarcheId();
        // Remplacement des espaces par des "+"...
        filename = filename.replace(" ", "+");
        LOGGER.info(FIN_FILE_CLIENT_GET_FILE, accountId, containerId, filename);
        afBackUtils.getFileClient().getFile(accountId, containerId, filename, response);
    }

    @Override
    public InputStream getFile(String filename, String containerId) throws IOException {
        LOGGER.info(DEBUT_FILE_SERVICE_GET_FILE, filename, containerId);
        String accountId = gouvPropertiesResolver.getDemarcheId();
        // Remplacement des espaces par des "+"...
        filename = filename.replace(" ", "+");
        InputStream is = afBackUtils.getFileClient().getFile(accountId, containerId, filename);
        LOGGER.info(FIN_FILE_CLIENT_GET_FILE, accountId, containerId, filename);
        return is;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InputStream> getFileEntity(String filename, String containerId) throws IOException {
        LOGGER.info(DEBUT_FILE_SERVICE_GET_FILE, filename, containerId);
        String accountId = gouvPropertiesResolver.getDemarcheId();
        // Remplacement des espaces par des "+"...
        filename = filename.replace(" ", "+");
        ResponseEntity<InputStream> fileEntity = afBackUtils.getFileClient()
                .getFileEntity(accountId, containerId, filename);
        LOGGER.info(FIN_FILE_CLIENT_GET_FILE, accountId, containerId, filename);
        return fileEntity;
    }

    @Override
    public InputStream getFile(String url) throws IOException {
        LOGGER.info("Début FileService.getFile({})", url);
        InputStream is = afBackUtils.getFileClient().getFile(url);
        LOGGER.info("Fin FileClient.getFile({})", url);
        return is;
    }

    @Override
    public String saveFile(DemandeDTO demande, String filename, String containerId, String contentType,
            InputStream inputStream, OutputStream outputStream) {

        LOGGER.info("FileService.saveFile({}, {}, {})", demande.getPkDemandes(), filename, contentType);

        // Définition de la meta pour le demande ID
        // On part du principe que le fichier a été généré côté back et n'est pas malicieux
        Map<String, String> customHeaders = createCustomHeaders(demande, true);

        filename = demande.getFkAccess() + SLASH_DELIMITER + UUID.randomUUID() + SLASH_DELIMITER + filename;

        LOGGER.info(FILENAME_DONNER_FILE_LOG_MESSAGE, filename);

        String accountId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info(FILECLIENT_SAVE_FILE_LOG_MESSAGE, accountId, containerId, filename);
        try {
            return afBackUtils.getFileClient()
                    .saveFile(accountId, containerId, inputStream, filename, contentType, customHeaders, outputStream);
        } catch (Exception e) {
            LOGGER.error(ERREUR_FILESERVICE_LOG_MESSAGE);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String saveFile(DemandeDTO demande, String containerId, MultipartFile file, HttpServletResponse response)
            throws IOException {
        String safeFileName = AfBackUtils.logSafe(file.getOriginalFilename());
        LOGGER.info("FileService.saveFile({}, {})", demande.getPkDemandes(), safeFileName);

        String fileNameToSave = FileNameUtils.getSafeFileName(file.getOriginalFilename());
        boolean vscanActivation = this.prepareSave(file, fileNameToSave);

        String filename = SLASH_DELIMITER + demande.getFkAccess() + SLASH_DELIMITER + UUID.randomUUID() + SLASH_DELIMITER
                + URLEncoder.encode(fileNameToSave, StandardCharsets.UTF_8);

        LOGGER.info(FILENAME_DONNER_FILE_LOG_MESSAGE, filename);

        Map<String, String> customHeaders = createCustomHeaders(demande, vscanActivation);

        String accountId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info(FILECLIENT_SAVE_FILE_LOG_MESSAGE, accountId, containerId, filename);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            return afBackUtils.getFileClient()
                    .saveFile(accountId, containerId, file.getInputStream(), filename, file.getContentType(),
                            customHeaders, outputStream);
        } catch (Exception e) {
            LOGGER.error(ERREUR_FILESERVICE_LOG_MESSAGE);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public String sendToFile(File tempFile, DemandeDTO demande, String fileName, boolean isPdf) throws IOException {
        LOGGER.info("Stockage du PDF généré dans FILE...");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(tempFile);
        String contentType = isPdf
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        String url = saveFile(demande, fileName, gouvPropertiesResolver.getContainerId(), contentType, fis,
                output);
        output.close();
        fis.close();
        return url;
    }

    @Override
    public String saveFilePublication(String codePublication, String containerId, MultipartFile file)
            throws IOException {
        String safeFileName = AfBackUtils.logSafe(file.getOriginalFilename());
        LOGGER.info("FileService.saveFilePublication({}, {})", codePublication, safeFileName);

        String fileNameToSave = FileNameUtils.getSafeFileName(file.getOriginalFilename());
        boolean vscanActivation = this.prepareSave(file, fileNameToSave);

        String filename = "/publications/" + UUID.randomUUID() + SLASH_DELIMITER + URLEncoder.encode(fileNameToSave,
                StandardCharsets.UTF_8);

        LOGGER.info(FILENAME_DONNER_FILE_LOG_MESSAGE, filename);

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put(FILE_METADATA_SCANEXECUTE, vscanActivation + "");

        String accountId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info(FILECLIENT_SAVE_FILE_LOG_MESSAGE, accountId, containerId, filename);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            return afBackUtils.getFileClient()
                    .saveFile(accountId, containerId, file.getInputStream(), filename, file.getContentType(),
                            customHeaders, outputStream);
        } catch (Exception e) {
            LOGGER.error(ERREUR_FILESERVICE_LOG_MESSAGE);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private boolean prepareSave(MultipartFile file, String fileNameToSave) throws IOException {
        // Vérification de l'extension du fichier
        if (!this.estExtensionDansWhitelist(fileNameToSave)) {
            LOGGER.info("Le type de fichier ne correspond pas aux types whitelistés ({}), pas d'upload dans FILE",
                    getExtensionsWhitelist());
            throw new FileUploadException("Erreur: le type du fichier soumis n'est pas valide",
                    FileUploadErrorEnum.EXTENSION_ERROR);
        }

        // Vérification de la taille maximum du fichier
        String maxFileSize = gouvPropertiesResolver.getMaxFileSize();
        if (maxFileSize == null || maxFileSize.isEmpty()) {
            throw new PropertyNotFoundException(
                    "La propriété obligatoire spring.servlet.multipart.max-file-size ne semble pas définie");
        }
        // Suppression de la partie "MB" pour récupérer uniquement le chiffre
        String numberPart = maxFileSize.replaceAll("\\D", "");

        // Conversion de la partie numérique en Long
        long tailleMaxFichier = Long.parseLong(numberPart);
        // transformation MB en B: 1 Mo = 1 048 576 octets
        long tailleMaxFichierB = tailleMaxFichier * 1048576;
        if (file.getSize() > tailleMaxFichierB) {
            throw new FileUploadException("Erreur: la taille du fichier transféré dépasse la limite autorisée",
                    FileUploadErrorEnum.TAILLE_MAX_ERROR);
        }

        // Vérification du vrai type MIME via Tika
        String mimeTypeFromExtension = new Tika().detect(fileNameToSave);

        try (InputStream inputStream = file.getInputStream();
                TikaInputStream tikaInputStream = TikaInputStream.get(inputStream)) {

            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileNameToSave);

            DefaultDetector detector = new DefaultDetector(new TikaConfig().getMimeRepository());
            org.apache.tika.mime.MediaType mediaType = detector.detect(tikaInputStream, metadata);
            String detectedMimeType = mediaType.toString();

            if (!mimeTypeFromExtension.equals(detectedMimeType)) {
                LOGGER.info("Le type MIME réel n'est pas celui de l'extension du fichier");
                throw new FileUploadException("Erreur: le type du fichier soumis n'est pas valide",
                        FileUploadErrorEnum.EXTENSION_ERROR);
            }
        } catch (TikaException e) {
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Appel à VSCAN pour vérifier la virulance du fichier
        boolean vscanActivation = gouvPropertiesResolver.isVscanActivated();

        LOGGER.info("Activation de VSCAN: {}", vscanActivation);
        if (vscanActivation) {
            ScanDTO scanDTO = verificationVSCAN(file);
            if (!scanDTO.isResult()) {
                LOGGER.info("VSCAN a détecté le fichier comme vérolé, fin du traitement, pas d'upload dans FILE");
                throw new VScanException("Erreur: le fichier soumis semble corrompu");
            }
            LOGGER.info("VSCAN n'a pas considéré le fichier soumis comme vérolé");
        }
        return vscanActivation;
    }

    private boolean estExtensionDansWhitelist(String filename) {
        if (StringUtils.isBlank(filename)) {
            return false;
        }
        String[] filenameSplit = filename.split("\\.");
        String fileExtension = filenameSplit[filenameSplit.length - 1].toLowerCase();
        return getExtensionsWhitelist().contains(fileExtension);
    }

    private List<String> getExtensionsWhitelist() {
        String extensionsProperty = gouvPropertiesResolver.getExtensionsWhitelist();
        List<String> extensions = new ArrayList<>();

        if (extensionsProperty != null && !extensionsProperty.isEmpty()) {
            String propertyString = extensionsProperty.replace("*.", "").replace(" ", "");
            String[] types = propertyString.split(",");
            Collections.addAll(extensions, types);
        }

        return extensions;
    }

    private Map<String, String> createCustomHeaders(DemandeDTO demande, boolean scanExecute) {
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put(FILE_METADATA_DEMANDEID, demande.getPkDemandes().toString());
        customHeaders.put(FILE_METADATA_DEMANDESTATUT, demande.getDernierStatut().getName());
        customHeaders.put(FILE_METADATA_SCANEXECUTE, scanExecute + "");
        return customHeaders;
    }

    public ScanDTO verificationVSCAN(MultipartFile file) throws IOException {
        LOGGER.info("Appel à VSCAN...");

        ObjectMapper mapper = new ObjectMapper();
        String urlVscan = gouvPropertiesResolver.getVScanUrl();
        LOGGER.info("URL = {}", urlVscan);
        HttpClient clientVscan = HttpClientBuilder.create().build();
        MultipartEntityBuilder builderVscan = MultipartEntityBuilder.create();
        builderVscan.addPart("file",
                new InputStreamBody(file.getInputStream(), ContentType.create(file.getContentType()), file.getName()));
        ScanRequestDTO scanRequest = new ScanRequestDTO();
        scanRequest.setCodeAppli(gouvPropertiesResolver.getDemarcheId());
        scanRequest.setFileName(file.getName());
        scanRequest.setEnduserAppModule(file.getName().toLowerCase() + "-frontserver");

        String scanRequestStr = mapper.writeValueAsString(scanRequest);
        builderVscan.addPart("scanRequest", new StringBody(scanRequestStr, ContentType.DEFAULT_TEXT));
        HttpEntity multipartVscan = builderVscan.build();
        HttpPost postRequestVscan = new HttpPost(urlVscan);
        postRequestVscan.setEntity(multipartVscan);
        postRequestVscan.addHeader(org.apache.http.HttpHeaders.AUTHORIZATION,
                AUTHORIZATION_PREFIX + gouvPropertiesResolver.getVscanJwt());
        HttpResponse postResponseVscan = clientVscan.execute(postRequestVscan);
        String vscanResp = IOUtils.toString(postResponseVscan.getEntity().getContent(), StandardCharsets.UTF_8);
        LOGGER.info("VSCAN Response : {} ({})", postResponseVscan.getStatusLine(), vscanResp);

        return mapper.readValue(vscanResp, ScanDTO.class);
    }

    private void initRestTemplate() {
        if (restTemplate == null) {
            LOGGER.info("Initialisation du RestTemplate...");
            try {
                restTemplate = new RestTemplate();
                List<HttpMessageConverter<?>> list = new ArrayList<>();
                MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter();
                List<MediaType> mediaTypes = new ArrayList<>();
                mediaTypes.add(new MediaType("application", "json", StandardCharsets.UTF_8));
                mediaTypes.add(new MediaType("text", "html", StandardCharsets.UTF_8));
                conv.setSupportedMediaTypes(mediaTypes);
                list.add(conv);
                restTemplate.setMessageConverters(list);
            } catch (Exception e) {
                LOGGER.error("FileServiceImpl() erreur : ", e);
            }
        }
    }

    private URL getFileURL(String fileurl) throws MalformedURLException {
        // file = accessId/uuid/filename (/uuid/filename inclu dans fichier.getUrl())
        if (fileurl.charAt(0) != '/') {
            fileurl = SLASH_DELIMITER + fileurl;
        }

        // Remplacer les espaces par des "+"...
        String filename = new File(fileurl).getName();
        fileurl = fileurl.replace(filename, filename.replace(" ", "+"));

        // Rajouter l'AccessID dans l'URL des fichiers

        String virtualPath =
                gouvPropertiesResolver.getDemarcheId() + SLASH_DELIMITER + gouvPropertiesResolver.getContainerId()
                        + SLASH_DELIMITER + fileurl;
        URL url = URI.create(gouvPropertiesResolver.getFileUrl() + SLASH_DELIMITER).resolve(virtualPath).toURL();
        LOGGER.info("URL du fichier calculée : {}", url);

        return url;
    }

    private Map<String, String> getFileMetadata(String fileUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(org.apache.http.HttpHeaders.AUTHORIZATION,
                AUTHORIZATION_PREFIX + gouvPropertiesResolver.getFileJwt());
        org.springframework.http.HttpEntity<Object> requestEntity = new org.springframework.http.HttpEntity<>(null,
                headers);
        ResponseEntity<Object> response = restTemplate.exchange(fileUrl, HttpMethod.HEAD, requestEntity, Object.class);
        HttpStatusCode httpStatus = response.getStatusCode();
        if (httpStatus != HttpStatus.OK) {
            throw new DemarchesServiceException("La requête HEAD a retourné le httpStatus " + httpStatus,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response.getHeaders().toSingleValueMap().entrySet().stream()
                // On ne retourne que les métadata du fichier
                .filter(entry -> entry.getKey().startsWith(FileUtils.MC_METADATA_PREFIX))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void updateFileMetadataGeneric(String fileUrl, String metadata, String value) {
        // Appel du getFile pour avoir les anciennes metadonnées
        Map<String, String> oldMetadatas = getFileMetadata(fileUrl);

        // Création du header avec les anciennes metadonnées
        HttpHeaders headers = new HttpHeaders();
        if (!oldMetadatas.isEmpty()) {
            oldMetadatas.forEach(headers::add);
        }

        // On met (ou remplace) dans le header la métadonnée qui contient la nouvelle metadonnée
        if (headers.containsKey(metadata)) {
            headers.set(metadata, value);
        } else {
            headers.add(metadata, value);
        }

        // Hack nécessaire parce que la méthode PATCH n'est pas forcément prise en compte par les couches sous
        // Spring (JDK 1.7)
        // Du coup on envoie en POST et FILE intercepte ce header dans un ServletFilter afin de placer le PATCH
        // qu'il faut
        headers.add(DemarchesUtils.METADATA_HTTPMETHODOVERRIDE, "PATCH");

        // Ajout de l'authentification JWT
        headers.add(org.apache.http.HttpHeaders.AUTHORIZATION,
                AUTHORIZATION_PREFIX + gouvPropertiesResolver.getFileJwt());

        // Pas de corps, mais des headers en guise de métadonnées
        org.springframework.http.HttpEntity<Object> requestEntity = new org.springframework.http.HttpEntity<>(null,
                headers);

        LOGGER.info("Appel à {}", fileUrl);

        ResponseEntity<Object> response = restTemplate.exchange(fileUrl, HttpMethod.POST, requestEntity, Object.class);
        HttpStatusCode httpStatus = response.getStatusCode();

        if (httpStatus != HttpStatus.OK) {
            throw new DemarchesServiceException("La requête PATCH a retourné le httpStatus " + httpStatus,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     */
    @Override
    public void updateFilesMetadataWithDemandeId(DemandeFileDTO[] fichiers, Integer demandeId) throws IOException {
        LOGGER.info("Début updateFilesMetadataWithDemandeId()");
        initRestTemplate();
        for (DemandeFileDTO fichier : fichiers) {
            URL url = getFileURL(fichier.getUrl());
            updateFileMetadataGeneric(url.toString(), FILE_METADATA_DEMANDEID, demandeId.toString());
        }
        LOGGER.info("Fin updateFilesMetadataWithDemandeId()");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     */
    @Override
    public void updateFileMetadata(String fichierURL, String metaKey, String metaValue) throws IOException {
        initRestTemplate();
        URL url = getFileURL(fichierURL);
        updateFileMetadataGeneric(url.toString(), metaKey, metaValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFile(String containerId, String fileName) {
        String accountId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info("Début suppression du fichier : {} sur la démarche : {}", fileName, accountId);
        try {
            afBackUtils.getFileClient().deleteFile(accountId, containerId, fileName);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la suppression du fichier : {}", fileName);
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("Fin suppression du fichier : {}", fileName);


    }

    @Override
    public void deleteFiles(String containerId, List<String> fileList) {
        if (CollectionUtils.isEmpty(fileList)) {
            LOGGER.info("La liste des fichiers à supprimer est vide. Pas d'appel à FILE");
            return;
        }
        String accountId = gouvPropertiesResolver.getDemarcheId();
        FileBatchDTO fileBatchDTO = new FileBatchDTO();
        // Remplacement des espaces par des "+" sur le nom des fichiers
        List<String> files = fileList.stream().map(file -> StringUtils.replace(file, StringUtils.SPACE, "+")).toList();
        fileBatchDTO.setFiles(files);
        fileBatchDTO.setAccount(accountId);
        fileBatchDTO.setContainer(containerId);
        try {
            afBackUtils.getFileClient().deleteFiles(accountId, containerId, fileBatchDTO);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la suppression du batch de fichiers : {}", StringUtils.join(fileList, "-"));
        }

    }

    @Override
    public boolean isFileDeletable(String fileUrl) {
        int existingFiles = demandesFilesRepository.countByUrl(fileUrl);
        int existingFilesBrouillons = brouillonsFilesRepository.countByUrl(fileUrl);
        if (existingFiles <= 1 && existingFilesBrouillons == 0) {
            return true;
        }
        LOGGER.info(MESSAGE_FICHIER_REFERENCE, fileUrl);
        return false;
    }

    @Override
    public boolean isFileBrouillonDeletable(String fileUrl) {
        int existingFiles = demandesFilesRepository.countByUrl(fileUrl);
        int existingFilesBrouillons = brouillonsFilesRepository.countByUrl(fileUrl);
        if (existingFiles == 0 && existingFilesBrouillons <= 1) {
            return true;
        }
        LOGGER.info(MESSAGE_FICHIER_REFERENCE, fileUrl);
        return false;
    }

    @Override
    public boolean isFileFromBrouillonDeletable(String fileUrl) {
        int existingFiles = demandesFilesRepository.countByUrl(fileUrl);
        int existingFilesCourriers = demandesCourriersRepository.countByUrl(fileUrl);
        int existingFilesComplements = demandesComplementsFilesRepository.countByUrl(fileUrl);
        int existingFilesBrouillons = brouillonsFilesRepository.countByUrl(fileUrl);
        if (existingFiles == 0 && existingFilesCourriers == 0 && existingFilesComplements == 0
                && existingFilesBrouillons <= 1) {
            return true;
        }
        String logSafe = AfBackUtils.logSafe(fileUrl);
        LOGGER.info(MESSAGE_FICHIER_REFERENCE, logSafe);
        return false;
    }

}
