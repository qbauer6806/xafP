package mc.gouv.xaf.back.service.itg.file.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import org.apache.http.client.ClientProtocolException;
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
        String virtualPath = account + "/" + container + "/" + filename;
        URL url = new URL(serviceUrl + "/" + virtualPath);
        LOGGER.info("URL d'appel : {}", url);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(url.toString());
        getRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());
        LOGGER.info("Appel du WS FILE");
        HttpResponse getResponse = client.execute(getRequest);
        return getResponse.getEntity().getContent();
    }

    public void getFile(String account, String container, String filename, HttpServletResponse response)
            throws ClientProtocolException, IOException {
        String virtualPath = account + "/" + container + "/" + filename;
        URL url = new URL(serviceUrl + "/" + virtualPath);
        LOGGER.info("URL d'appel : {}", url);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(url.toString());
        getRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());
        LOGGER.info("Appel du WS FILE");
        HttpResponse getResponse = client.execute(getRequest);

        LOGGER.info("Constitution de la réponse pour retour au client");
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

    public InputStream getFile(String fileurl) throws ClientProtocolException, IOException {

        // Constitution de la requête
        URL url = new URL(serviceUrl + "/" + fileurl);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(url.toString());

        getRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info("Appel du WS FILE");
        HttpResponse getResponse = client.execute(getRequest);

        return getResponse.getEntity().getContent();

    }

    public String saveFile(String account, String container, InputStream inputStream, String filename,
            String contentType, Map<String, String> customHeaders, OutputStream outputStream) throws Exception {

        // Constitution du chemin virtuel du fichier
        // /appfactory/demarcheId/accessId/UUID/nomDuFichier
        String virtualPath = account + "/" + container + "/" + filename;
        LOGGER.info("Chemin virtuel : {}", virtualPath);

        // Constitution de l'URL d'appel
        URL url = new URL(serviceUrl + "/" + virtualPath);
        LOGGER.info("URL d'appel : {}", url);

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();

        LOGGER.info("Envoyer " + filename);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("data", new InputStreamBody(inputStream, contentType, filename));
        HttpEntity multipart = builder.build();
        HttpPost postRequest = new HttpPost(url.toString());
        postRequest.setEntity(multipart);

        // Si le client a fourni des métadonnées (en X-MC-*), alors les transmettre à FILE
        if (customHeaders != null) {
            for (String headerName : customHeaders.keySet()) {
                if (headerName.startsWith(MC_METADATA_PREFIX)) {
                    postRequest.setHeader(headerName, customHeaders.get(headerName));
                }
            }
        }
        postRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info("Appel du WS FILE");
        HttpResponse postResponse = client.execute(postRequest);

        int statusCode = postResponse.getStatusLine().getStatusCode();

        if (statusCode != HttpStatus.SC_CREATED) {
            String strResp = IOUtils.toString(postResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(strResp);
            JsonNode errorsNode = root.get("errors");
            String errorMessage =
                    errorsNode != null && errorsNode.isArray() && !errorsNode.isEmpty() ? errorsNode.get(0)
                            .get("libelle").asText() : "Erreur lors de l'enregistrement du fichier";
            throw new Exception(errorMessage);
        }

        postResponse.getEntity().writeTo(outputStream);

        return filename;
    }

    public String saveFile(String account, String container, Part part, String filename,
            Map<String, String> customHeaders, HttpServletResponse response) throws IOException, ServletException {

        // Constitution du chemin virtuel du fichier
        // /appfactory/demarcheId/accessId/UUID/nomDuFichier
        String virtualPath = account + "/" + container + "/" + filename;
        LOGGER.info("Chemin virtuel : {}", virtualPath);

        // Constitution de l'URL d'appel
        URL url = new URL(serviceUrl + "/" + virtualPath);
        LOGGER.info("URL d'appel : {}", url);

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();

        LOGGER.info("Envoyer " + AfBackUtils.logSafe(part.getSubmittedFileName()));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("data",
                new InputStreamBody(part.getInputStream(), part.getContentType(), part.getSubmittedFileName()));
        HttpEntity multipart = builder.build();
        HttpPost postRequest = new HttpPost(url.toString());
        postRequest.setEntity(multipart);

        // Si le client a fourni des métadonnées (en X-MC-*), alors les transmettre à FILE
        for (String headerName : customHeaders.keySet()) {
            if (headerName.startsWith(MC_METADATA_PREFIX)) {
                postRequest.setHeader(headerName, customHeaders.get(headerName));
            }
        }

        postRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info("Appel du WS FILE");
        HttpResponse postResponse = client.execute(postRequest);

        // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
        LOGGER.info("Constitution de la réponse pour retour au client");
        response.setContentType("application/json");

        int statusCode = postResponse.getStatusLine().getStatusCode();
        response.setStatus(statusCode);

        IOUtils.copy(postResponse.getEntity().getContent(), response.getOutputStream());
        return filename;
    }

    public List<FileDTO> getContainerFileList(String account, String container) throws Exception {
        String virtualPath = account + "/" + container;
        URL url = new URL(serviceUrl + "/" + virtualPath);
        LOGGER.info("URL d'appel : {}", url);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(url.toString());
        getRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());
        LOGGER.info("Appel du WS FILE");
        HttpResponse getResponse = client.execute(getRequest);

        LOGGER.info("Constitution de la réponse pour retour au client");
        int statusCode = getResponse.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            String strResp = IOUtils.toString(getResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(strResp);
            JsonNode errorsNode = root.get("errors");
            String errorMessage =
                    errorsNode != null && errorsNode.isArray() && !errorsNode.isEmpty() ? errorsNode.get(0)
                            .get("libelle").asText() : "Erreur lors de la récupération de la liste des fichiers.";
            throw new Exception(errorMessage);
        }
        FileDTO[] fileDtos = objectMapper.readValue(getResponse.getEntity().getContent(), FileDTO[].class);
        return Arrays.asList(fileDtos);
    }

    public void deleteFile(String account, String container, String filename) throws Exception {
        String virtualPath = account + "/" + container + "/" + filename;
        URL url = new URL(serviceUrl + "/" + virtualPath);
        LOGGER.info("URL d'appel : {}", url);

        HttpClient client = HttpClientBuilder.create().build();
        HttpDelete deleteRequest = new HttpDelete(url.toString());
        deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());
        LOGGER.info("Appel du WS FILE");
        HttpResponse deleteResponse = client.execute(deleteRequest);

        LOGGER.info("Constitution de la réponse pour retour au client");
        int statusCode = deleteResponse.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            String strResp = IOUtils.toString(deleteResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(strResp);
            JsonNode errorsNode = root.get("errors");
            String errorMessage =
                    errorsNode != null && errorsNode.isArray() && !errorsNode.isEmpty() ? errorsNode.get(0)
                            .get("libelle").asText() : "Erreur lors de la suppression du fichier.";
            throw new Exception(errorMessage);
        }
    }

    static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";
        public String getMethod() { return METHOD_NAME; }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }
    }

    public FileBatchResponseDTO deleteFiles(String account, String container, FileBatchDTO batchDto)
            throws Exception {

        // Constitution de l'URL d'appel
        String virtualPath = account + "/" + container + "/batch";
        URL url = new URL(serviceUrl + "/" + virtualPath);
        LOGGER.info("URL d'appel : {}", url);

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpDeleteWithBody deleteRequest = new HttpDeleteWithBody(url.toString());

        HttpEntity entity = new StringEntity(objectMapper.writeValueAsString(batchDto), ContentType.APPLICATION_JSON);
        deleteRequest.setEntity(entity);
        deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info("Appel du WS FILE");
        HttpResponse deleteResponse = client.execute(deleteRequest);

        LOGGER.info("Constitution de la réponse pour retour au client");
        int statusCode = deleteResponse.getStatusLine().getStatusCode();
        String strResp = IOUtils.toString(deleteResponse.getEntity().getContent(), StandardCharsets.UTF_8);

        if (statusCode != HttpStatus.SC_OK) {
            JsonNode root = objectMapper.readTree(strResp);
            JsonNode errorsNode = root.get("errors");
            String errorMessage =
                    errorsNode != null && errorsNode.isArray() && !errorsNode.isEmpty() ? errorsNode.get(0)
                            .get("libelle").asText() : "Erreur lors de la suppression des fichiers.";
            throw new Exception(errorMessage);
        }
        return objectMapper.readValue(strResp, FileBatchResponseDTO.class);
    }

    private String getAuthHeader() {
        return "Bearer " + jwt;
    }
}
