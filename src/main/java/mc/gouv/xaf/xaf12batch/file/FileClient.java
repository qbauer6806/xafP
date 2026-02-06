package mc.gouv.xaf.xaf12batch.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Classe cliente permettant d'appeler le WS FILE
 *
 * @author qdeme
 *
 */
@Setter
@Getter
@Component
public class FileClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileClient.class);
    private static final String SLASH = "/";
    private static final String ERRORS = "errors";
    private static final String LIBELLE = "libelle";
    private static final String APPEL_WS_FILE = "Appel du WS FILE";

    @Value("${file.url}")
    private String serviceUrl;

    @Value("${file.jwt}")
    private String jwt;
    private static final String MC_METADATA_PREFIX = "X-MC-";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CloseableHttpClient client;

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
        HttpGet request = new HttpGet(uri);
        request.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());
        LOGGER.info("getFileEntity : Appel du WS FILE");

        final CloseableHttpResponse response = client.execute(request);
        final int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode != HttpStatus.SC_OK) {
            try {
                EntityUtils.consumeQuietly(response.getEntity());
            } finally {
                response.close();
            }
            LOGGER.warn("Fichier introuvable pour le chemin : {}", virtualPath);
            return ResponseEntity.notFound().build();
        }

        // l'inputstream est lié à la connexion
        final InputStream raw = response.getEntity().getContent();

        InputStream wrapped = new FilterInputStream(raw) {

            @Override
            public void close() throws IOException {
                // fermer le stream et fermer la réponse pour libérer la connexion
                try {
                    super.close();
                } finally {
                    response.close();
                }
            }
        };

        // recopier content-type/content-length si présents
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        Header ct = response.getEntity().getContentType();
        if (ct != null && ct.getValue() != null) {
            builder.header(HttpHeaders.CONTENT_TYPE, ct.getValue());
        }
        long len = response.getEntity().getContentLength();
        if (len >= 0) {
            builder.contentLength(len);
        }

        return builder.body(wrapped);
    }

    private URI createUrl(String virtualPath) {
        return UriComponentsBuilder.fromHttpUrl(serviceUrl)
                .path(SLASH)
                .path(virtualPath)
                .build()
                .toUri();
    }

    public InputStream getFile(String fileurl) throws IOException {

        // Constitution de la requête
        URI uri = this.createUrl(fileurl);
        return this.getFile(uri);

    }

    @Bean
    public CloseableHttpClient fileHttpClient() {
        PoolingHttpClientConnectionManager cm =
                new PoolingHttpClientConnectionManager();

        cm.setMaxTotal(50);
        cm.setDefaultMaxPerRoute(20);

        return HttpClients.custom()
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .build();
    }

    public InputStream getFile(URI uri) throws IOException {
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

        // Constitution de l'URL d'appel
        URI uri = this.createUrl(virtualPath);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("data", new InputStreamBody(inputStream, ContentType.create(contentType), filename));
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
        try (CloseableHttpResponse postResponse =
                client.execute(postRequest)) {

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
        }

        return filename;
    }



    private String getAuthHeader() {
        return "Bearer " + jwt;
    }
}
