package mc.gouv.xaf.back.service.itg.file.service;
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

import mc.gouv.xaf.back.service.itg.file.service.dto.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * Classe cliente permettant d'appeler le WS FILE
 *
 * @author qdeme
 *
 */
public class FileClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileClient.class);

    private String serviceUrl;

    private String user;

    private String password;

    private String jwt;

    private static final String MC_METADATA_PREFIX = "X-MC-";

    /**
     * Crée une instance du client avec authentification Basic
     *
     * @param serviceUrl
     *            URL du WS à appeler
     * @param user
     *            User à utiliser pour l'authentification
     * @param password
     *            Mot de passe à utiliser pour l'authentification
     */
    public FileClient(String serviceUrl, String user, String password) {
        this.serviceUrl = serviceUrl;
        this.user = user;
        this.password = password;
    }

    /**
     * Crée une instance du client avec authentification JWT
     *
     * @param serviceUrl
     * @param jwt
     */
    public FileClient(String serviceUrl, String jwt) {
        this.serviceUrl = serviceUrl;
        this.jwt = jwt;
    }

    /**
     * Fonction retournant un InputStream
     *
     * @param account
     * @param container
     * @param filename
     * @throws ClientProtocolException
     * @throws IOException
     */
    public InputStream getFile(String account, String container, String filename)
            throws IOException {

        // Constitution de l'URL d'appel
        String virtualPath = account + "/" + container + "/" + filename;
        URL url = new URL(serviceUrl + "/" + virtualPath);
        LOGGER.info("URL d'appel : {}", url);

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(url.toString());

        getRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info("Appel du WS FILE");
        HttpResponse getResponse = client.execute(getRequest);

        return getResponse.getEntity().getContent();
    }

    /**
     * Fonction pour appel par une Servlet. Retour contenu dans le HttpServletResponse
     *
     * @param account
     * @param container
     * @param filename
     * @param response
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void getFile(String account, String container, String filename, HttpServletResponse response)
            throws ClientProtocolException, IOException {

        // Constitution de l'URL d'appel
        String virtualPath = account + "/" + container + "/" + filename;
        URL url = new URL(serviceUrl + "/" + virtualPath);
        LOGGER.info("URL d'appel : {}", url);

        // Constitution de la requête
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

        // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
        LOGGER.info("Constitution de la réponse pour retour au client");

        // TODO #5764 Error handling : exceptions XBoot avec ErrorDTO à la place de l'implémentation custom actuelle (+
        // gestion dans l'apiclient)
        int statusCode = postResponse.getStatusLine().getStatusCode();

        if (statusCode != HttpStatus.SC_CREATED) {
            JSONParser jsonParser = new JSONParser();
            String strResp = IOUtils.toString(postResponse.getEntity().getContent(), StandardCharsets.UTF_8.name());
            JSONObject json = (JSONObject)jsonParser.parse(strResp);
            JSONArray array = (JSONArray)json.get("errors");
            String errorMessage = (String)((JSONObject)array.get(0)).get("libelle");
            throw new Exception(errorMessage);
        }

        postResponse.setStatusCode(statusCode);
        // String theString = IOUtils.toString(postResponse.getEntity().getContent(), "UTF-8");

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

        LOGGER.info("Envoyer " + part.getSubmittedFileName());

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
        // Constitution de l'URL d'appel
        String virtualPath = account + "/" + container;
        URL url = new URL(serviceUrl + "/" + virtualPath);
        LOGGER.info("URL d'appel : {}", url);

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(url.toString());

        getRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info("Appel du WS FILE");
        HttpResponse getResponse = client.execute(getRequest);

        ObjectMapper objectMapper = new ObjectMapper();

        LOGGER.info("Constitution de la réponse pour retour au client");
        if (getResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            JSONParser jsonParser = new JSONParser();
            String strResp = IOUtils.toString(getResponse.getEntity().getContent(), StandardCharsets.UTF_8.name());
            JSONObject json = (JSONObject)jsonParser.parse(strResp);
            JSONArray array = (JSONArray)json.get("errors");
            String errorMessage = (String)((JSONObject)array.get(0)).get("libelle");
            throw new Exception(errorMessage);
        }
        System.out.println("resp=" + getResponse.getEntity().getContent());
        FileDTO[] fileDtos = objectMapper.readValue(getResponse.getEntity().getContent(), FileDTO[].class);

        return Arrays.asList(fileDtos);
    }

    public void deleteFile(String account, String container, String filename)
            throws Exception {

        // Constitution de l'URL d'appel
        String virtualPath = account + "/" + container + "/" + filename;
        URL url = new URL(serviceUrl + "/" + virtualPath);
        LOGGER.info("URL d'appel : {}", url);

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpDelete deleteRequest = new HttpDelete(url.toString());

        deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info("Appel du WS FILE");
        HttpResponse deleteResponse = client.execute(deleteRequest);

        LOGGER.info("Constitution de la réponse pour retour au client");
        if (deleteResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            JSONParser jsonParser = new JSONParser();
            String strResp = IOUtils.toString(deleteResponse.getEntity().getContent(), StandardCharsets.UTF_8.name());
            JSONObject json = (JSONObject)jsonParser.parse(strResp);
            JSONArray array = (JSONArray)json.get("errors");
            String errorMessage = (String)((JSONObject)array.get(0)).get("libelle");
            throw new Exception(errorMessage);
        }
    }

    class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";
        public String getMethod() { return METHOD_NAME; }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }
        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }
        public HttpDeleteWithBody() { super(); }
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

        ObjectMapper objectMapper = new ObjectMapper();
        HttpEntity entity = new StringEntity(objectMapper.writeValueAsString(batchDto), ContentType.APPLICATION_JSON);
        deleteRequest.setEntity(entity);

        deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        LOGGER.info("Appel du WS FILE");
        HttpResponse deleteResponse = client.execute(deleteRequest);

        LOGGER.info("Constitution de la réponse pour retour au client");
        if (deleteResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            JSONParser jsonParser = new JSONParser();
            String strResp = IOUtils.toString(deleteResponse.getEntity().getContent(), StandardCharsets.UTF_8.name());
            JSONObject json = (JSONObject)jsonParser.parse(strResp);
            JSONArray array = (JSONArray)json.get("errors");
            String errorMessage = (String)((JSONObject)array.get(0)).get("libelle");
            throw new Exception(errorMessage);
        }

        String strResp = IOUtils.toString(deleteResponse.getEntity().getContent(), StandardCharsets.UTF_8.name());
        return objectMapper.readValue(strResp, FileBatchResponseDTO.class);
    }


    // /**
    // * Définition de l'authentification Utilisation d'un AuthCache puis d'un Context que l'on donne au moment de
    // l'appel
    // * au serveur, afin de faire une authentification dès la première tentative, et non dès la deuxième tentative, car
    // * dans le deuxième cas, cela force à faire un retry et donc si on utilise un InputStream, étant donné qu'on ne
    // peut
    // * pas le lire deux fois, cela donnerait une NonRepeatableRequestException.
    // *
    // * @param url
    // * URL du service à appeler
    // * @return
    // */
    // private HttpClientContext getHttpContextForAuth(URL url) {
    //
    // LOGGER.info("Constitution de la requête...");
    // HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), "http");
    //
    // AuthCache authCache = new BasicAuthCache();
    // authCache.put(targetHost, new BasicScheme());
    //
    // // Ajout de l'AuthCache au contexte d'exécution
    // final HttpClientContext context = HttpClientContext.create();
    // CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    // credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
    // context.setCredentialsProvider(credentialsProvider);
    // context.setAuthCache(authCache);
    //
    // return context;
    //
    // }

    private String getAuthHeader() {
        if (!StringUtils.isBlank(jwt)) {
            // Authentification JWT
            return "Bearer " + jwt;
        } else {
            String auth = user + ":" + password;
            return "Basic " + new String(Base64.encodeBase64(auth.getBytes()));
        }
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

}
