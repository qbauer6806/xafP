package mc.gouv.xaf.back.service.itg.file.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.service.itg.file.service.dto.FileBatchDTO;
import mc.gouv.xaf.back.service.itg.file.service.dto.FileBatchResponseDTO;
import mc.gouv.xaf.back.service.itg.file.service.dto.FileDTO;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

/**
 * Classe cliente permettant d'appeler le WS FILE
 *
 * @author qdeme
 *
 */
@Setter
@Getter
public class FileClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileClient.class);
    private static final String SLASH = "/";
    private static final String ERRORS = "errors";
    private static final String LIBELLE = "libelle";
    private static final String URL_APPEL = "URL d'appel : {}";
    private static final String APPEL_WS_FILE = "Appel du WS FILE";
    private static final String CONSTITUTION_REPONSE_RETOUR_CLIENT = "Constitution de la réponse pour retour au client";

    private String serviceUrl;
    private String jwt;
    private static final String MC_METADATA_PREFIX = "X-MC-";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public FileClient(String serviceUrl, String jwt) {
        this.serviceUrl = serviceUrl;
        this.jwt = jwt;
    }

    public InputStream getFile(String account, String container, String filename)
            throws IOException {
        String virtualPath = account + SLASH + container + SLASH + filename;
        URI uri = this.createUrl(virtualPath);
        LOGGER.info(URL_APPEL, uri);
        return this.getFile(uri);
    }

    /**
     * Récupère un fichier à partir du compte et du conteneur spécifiés.
     *
     * @param account
     *         le nom du compte associé au fichier
     * @param container
     *         le conteneur ou le répertoire du compte où le fichier est stocké
     * @param filename
     *         le nom du fichier à récupérer
     * @return un InputStream représentant le contenu du fichier
     * @throws IOException
     *         si une exception d’entrée/sortie se produit lors du processus de récupération du fichier
     */
    public ResponseEntity<InputStream> getFileEntity(String account, String container, String filename)
            throws IOException {
        String virtualPath = account + SLASH + container + SLASH + filename;
        URI uri = this.createUrl(virtualPath);
        LOGGER.info("getFileEntity : URL d'appel : {}", uri);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(uri);
        request.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());
        LOGGER.info("getFileEntity : Appel du WS FILE");
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK != statusCode) {
            LOGGER.warn("Fichier introuvable pour le chemin : {}", virtualPath);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(response.getEntity().getContent());
    }

    private URI createUrl(String virtualPath) {
        return URI.create(serviceUrl + SLASH).resolve(virtualPath);
    }

    public void getFile(String account, String container, String filename, HttpServletResponse response)
            throws IOException {
        String virtualPath = account + SLASH + container + SLASH + filename;
        URI uri = this.createUrl(virtualPath);
        LOGGER.info(URL_APPEL, uri);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(uri);
        getRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());
        LOGGER.info(APPEL_WS_FILE);
        HttpResponse getResponse = client.execute(getRequest);

        LOGGER.info(CONSTITUTION_REPONSE_RETOUR_CLIENT);
        response.setStatus(getResponse.getStatusLine().getStatusCode());
        response.setContentType(getResponse.getEntity().getContentType().getValue());

        // Ne pas dupliquer Content-Type et Date
        Set<String> headersToSkip = Set.of("Content-Type", "Date");

        // Copie des headers
        for (Header header : getResponse.getAllHeaders()) {
            if (!headersToSkip.contains(header.getName())) {
                response.addHeader(header.getName(), header.getValue());
            }
        }

        // Et en dernier on copie le stream... Car si on met les headers après, ils sont tous ignorés !
        IOUtils.copy(getResponse.getEntity().getContent(), response.getOutputStream());
    }

    public InputStream getFile(String fileurl) throws IOException {

        // Constitution de la requête
        URI uri = this.createUrl(fileurl);
        return this.getFile(uri);

    }

    public InputStream getFile(URI uri) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(uri);

        request.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info(APPEL_WS_FILE);
        HttpResponse response = client.execute(request);

        return response.getEntity().getContent();
    }

    public String saveFile(String account, String container, InputStream inputStream, String filename,
            String contentType, Map<String, String> customHeaders, OutputStream outputStream) throws IOException {

        // Constitution du chemin virtuel du fichier
        // /appfactory/demarcheId/accessId/UUID/nomDuFichier
        String virtualPath = account + SLASH + container + SLASH + filename;
        LOGGER.info("Chemin virtuel : {}", virtualPath);

        // Constitution de l'URL d'appel
        URI uri = this.createUrl(virtualPath);
        LOGGER.info(URL_APPEL, uri);

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();

        LOGGER.info("Envoyer {}", filename);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("data", new InputStreamBody(inputStream, ContentType.getByMimeType(contentType), filename));
        HttpEntity multipart = builder.build();
        HttpPost postRequest = new HttpPost(uri);
        postRequest.setEntity(multipart);

        // Si le client a fourni des métadonnées (en X-MC-*), alors les transmettre à FILE
        if (customHeaders != null) {
            for (Entry<String, String> header : customHeaders.entrySet()) {
                if (header.getKey().startsWith(MC_METADATA_PREFIX)) {
                    postRequest.setHeader(header.getKey(), header.getValue());
                }
            }
        }
        postRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info(APPEL_WS_FILE);
        HttpResponse postResponse = client.execute(postRequest);

        int statusCode = postResponse.getStatusLine().getStatusCode();

        if (statusCode != HttpStatus.SC_CREATED) {
            String strResp = IOUtils.toString(postResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(strResp);
            JsonNode errorsNode = root.get(ERRORS);
            String errorMessage =
                    errorsNode != null && errorsNode.isArray() && !errorsNode.isEmpty() ? errorsNode.get(0).get(LIBELLE)
                            .asText() : "Erreur lors de l'enregistrement du fichier";
            throw new IOException(errorMessage);
        }

        postResponse.getEntity().writeTo(outputStream);

        return filename;
    }

    public String saveFile(String account, String container, Part part, String filename,
            Map<String, String> customHeaders, HttpServletResponse response) throws IOException {

        // Constitution du chemin virtuel du fichier
        // /appfactory/demarcheId/accessId/UUID/nomDuFichier
        String virtualPath = account + SLASH + container + SLASH + filename;
        LOGGER.info("Chemin virtuel : {}", virtualPath);

        // Constitution de l'URL d'appel
        URI uri = this.createUrl(virtualPath);
        LOGGER.info(URL_APPEL, uri);

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();

        String logSafe = AfBackUtils.logSafe(part.getSubmittedFileName());
        LOGGER.info("Envoyer {}", logSafe);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("data",
                new InputStreamBody(part.getInputStream(), ContentType.getByMimeType(part.getContentType()),
                        part.getSubmittedFileName()));
        HttpEntity multipart = builder.build();
        HttpPost postRequest = new HttpPost(uri);
        postRequest.setEntity(multipart);

        // Si le client a fourni des métadonnées (en X-MC-*), alors les transmettre à FILE
        for (Entry<String, String> header : customHeaders.entrySet()) {
            if (header.getKey().startsWith(MC_METADATA_PREFIX)) {
                postRequest.setHeader(header.getKey(), header.getValue());
            }
        }

        postRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info(APPEL_WS_FILE);
        HttpResponse postResponse = client.execute(postRequest);

        // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
        LOGGER.info(CONSTITUTION_REPONSE_RETOUR_CLIENT);
        response.setContentType("application/json");

        int statusCode = postResponse.getStatusLine().getStatusCode();
        response.setStatus(statusCode);

        IOUtils.copy(postResponse.getEntity().getContent(), response.getOutputStream());
        return filename;
    }

    public List<FileDTO> getContainerFileList(String account, String container) throws IOException {
        String virtualPath = account + SLASH + container;
        URI uri = this.createUrl(virtualPath);
        LOGGER.info(URL_APPEL, uri);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(uri);
        getRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());
        LOGGER.info(APPEL_WS_FILE);
        HttpResponse getResponse = client.execute(getRequest);

        LOGGER.info(CONSTITUTION_REPONSE_RETOUR_CLIENT);
        int statusCode = getResponse.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            String strResp = IOUtils.toString(getResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(strResp);
            JsonNode errorsNode = root.get(ERRORS);
            String errorMessage =
                    errorsNode != null && errorsNode.isArray() && !errorsNode.isEmpty() ? errorsNode.get(0).get(LIBELLE)
                            .asText() : "Erreur lors de la récupération de la liste des fichiers.";
            throw new IOException(errorMessage);
        }
        FileDTO[] fileDtos = objectMapper.readValue(getResponse.getEntity().getContent(), FileDTO[].class);
        return Arrays.asList(fileDtos);
    }

    public void deleteFile(String account, String container, String filename) throws IOException {
        String virtualPath = account + SLASH + container + SLASH + filename;
        URI uri = this.createUrl(virtualPath);
        LOGGER.info(URL_APPEL, uri);

        HttpClient client = HttpClientBuilder.create().build();
        HttpDelete deleteRequest = new HttpDelete(uri);
        deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());
        LOGGER.info(APPEL_WS_FILE);
        HttpResponse deleteResponse = client.execute(deleteRequest);

        LOGGER.info(CONSTITUTION_REPONSE_RETOUR_CLIENT);
        int statusCode = deleteResponse.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            String strResp = IOUtils.toString(deleteResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(strResp);
            JsonNode errorsNode = root.get(ERRORS);
            String errorMessage =
                    errorsNode != null && errorsNode.isArray() && !errorsNode.isEmpty() ? errorsNode.get(0).get(LIBELLE)
                            .asText() : "Erreur lors de la suppression du fichier.";
            throw new IOException(errorMessage);
        }
    }

    static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";
        public String getMethod() { return METHOD_NAME; }

        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }
    }

    public FileBatchResponseDTO deleteFiles(String account, String container, FileBatchDTO batchDto)
            throws IOException {

        // Constitution de l'URL d'appel
        String virtualPath = account + SLASH + container + "/batch";
        URI uri = this.createUrl(virtualPath);
        LOGGER.info(URL_APPEL, uri);

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpDeleteWithBody deleteRequest = new HttpDeleteWithBody(uri);

        HttpEntity entity = new StringEntity(objectMapper.writeValueAsString(batchDto), ContentType.APPLICATION_JSON);
        deleteRequest.setEntity(entity);
        deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info(APPEL_WS_FILE);
        HttpResponse deleteResponse = client.execute(deleteRequest);

        LOGGER.info(CONSTITUTION_REPONSE_RETOUR_CLIENT);
        int statusCode = deleteResponse.getStatusLine().getStatusCode();
        String strResp = IOUtils.toString(deleteResponse.getEntity().getContent(), StandardCharsets.UTF_8);

        if (statusCode != HttpStatus.SC_OK) {
            JsonNode root = objectMapper.readTree(strResp);
            JsonNode errorsNode = root.get(ERRORS);
            String errorMessage =
                    errorsNode != null && errorsNode.isArray() && !errorsNode.isEmpty() ? errorsNode.get(0).get(LIBELLE)
                            .asText() : "Erreur lors de la suppression des fichiers.";
            throw new IOException(errorMessage);
        }
        return objectMapper.readValue(strResp, FileBatchResponseDTO.class);
    }

    private String getAuthHeader() {
        return "Bearer " + jwt;
    }
}
