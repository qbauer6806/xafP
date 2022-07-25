package mc.gouv.xaf.back.paiement.client.rio;

import mc.gouv.xaf.back.paiement.dto.itg.rio.DocumentDTO;
import mc.gouv.xaf.back.paiement.dto.itg.rio.DocumentRequestDTO;
import mc.gouv.xaf.back.paiement.dto.itg.rio.FileDocumentDTO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.client.RioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
public class RioClientImpl implements RioClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RioClientImpl.class);

    // Entrypoint
    private static String url;
    private static String jwt;

    // Endpoints
    public static final String RIO_CREATE_DOCUMENT = "/documents/%s";
    public static final String RIO_GET_DOCUMENT = "/documents/%s/%s/notices/%s";
    public static final String RIO_DELETE_DOCUMENT = "/documents/%s/%s/notices/%s";
    public static final String RIO_CREATE_FILE_DOCUMENT = "/documents/%s/%s/%s/notices/%s/newfile";
    public static final String RIO_GET_FILE_DOCUMENT = "/documents/%s/%s/notices/%s/files/%s";

    @Autowired
    private PaiementPropertiesResolver paiementPropertiesResolver;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @PostConstruct
    @SuppressWarnings("squid:S2696")
    private void setUp() {
        url = paiementPropertiesResolver.getApiRioUrl();
        jwt = paiementPropertiesResolver.getApiRioJwt();
    }

    @Override
    public DocumentDTO createDocument(String codeAppli, String lastModifier, String codeNotice, String refDocument) {

        LOGGER.info("Création du document {}", refDocument);

        // TODO Méthode à tester lors de l'intégration

        RestTemplate rest = getRestTemplate();

        HttpHeaders headers = getRioRequestHeaders();
        DocumentRequestDTO documentRqDTO = new DocumentRequestDTO();
        documentRqDTO.setCodeApplication(codeAppli);
        documentRqDTO.setLastModifier(lastModifier);
        documentRqDTO.setCodeNotice(codeNotice);
        HttpEntity<DocumentRequestDTO> requestEntity = new HttpEntity<>(documentRqDTO, headers);

        String requestUrl = url + String.format(RIO_CREATE_DOCUMENT, refDocument);
        URI uri = UriComponentsBuilder
                .fromHttpUrl(requestUrl)
                .build().encode().toUri();

        ResponseEntity<DocumentDTO> responseEntity = rest.exchange(uri, HttpMethod.POST, requestEntity, DocumentDTO.class);

        LOGGER.info("Fin création du document {}", refDocument);

        return responseEntity.getBody();
    }

    @Override
    public DocumentDTO getDocument(String codeAppli, String refDocument, String codeNotice, String user) {

        LOGGER.info("Récupération du document {}", refDocument);

        RestTemplate rest = getRestTemplate();

        HttpHeaders headers = getRioRequestHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        String requestUrl = url + String.format(RIO_GET_DOCUMENT, codeAppli, refDocument, codeNotice);
        URI uri = UriComponentsBuilder
                .fromHttpUrl(requestUrl)
                .queryParam("user", user)
                .build().encode().toUri();

        ResponseEntity<DocumentDTO> responseEntity = rest.exchange(uri, HttpMethod.GET, requestEntity, DocumentDTO.class);

        LOGGER.info("Fin récupération du document {}", refDocument);

        return responseEntity.getBody();
    }

    @Override
    public DocumentDTO deleteDocument(String codeAppli, String refDocument, String codeNotice, String user) {

        LOGGER.info("Suppression du document {}", refDocument);

        // TODO Méthode à tester lors de l'intégration

        RestTemplate rest = getRestTemplate();

        HttpHeaders headers = getRioRequestHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        String requestUrl = url + String.format(RIO_DELETE_DOCUMENT, codeAppli, refDocument, codeNotice);
        URI uri = UriComponentsBuilder
                .fromHttpUrl(requestUrl)
                .queryParam("user", user)
                .build().encode().toUri();

        ResponseEntity<DocumentDTO> responseEntity = rest.exchange(uri, HttpMethod.DELETE, requestEntity, DocumentDTO.class);

        LOGGER.info("Fin suppression du document {}", refDocument);

        return responseEntity.getBody();
    }

    @Override
    public FileDocumentDTO createFileDocument(String codeAppli, String refDocument, Long keyDocument, String codeNotice, String user, String filename, byte[] file) {

        LOGGER.info("Création du fichier (filename) {} pour le document {}", filename, refDocument);

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("filename", filename);
        parts.add("file", file);

        HttpHeaders headers = getRioMultipartRequestHeaders();
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

        RestTemplate rest = restTemplateBuilder.build();
        rest.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8));

        String requestUrl = url + String.format(RIO_CREATE_FILE_DOCUMENT, codeAppli, refDocument, keyDocument, codeNotice);
        URI uri = UriComponentsBuilder
                .fromHttpUrl(requestUrl)
                .queryParam("user", user)
                .build().encode().toUri();

        ResponseEntity<FileDocumentDTO> responseEntity = rest.exchange(uri, HttpMethod.POST, requestEntity, FileDocumentDTO.class);

        LOGGER.info("Fin création du fichier (filename) {} pour le document {}", filename, refDocument);

        return responseEntity.getBody();
    }

    @Override
    public FileDocumentDTO getFileDocument(String codeAppli, String refDocument, Integer keyFile, String codeNotice, String user) {

        LOGGER.info("Récupération du fichier (keyfile) {} pour le document {}", keyFile, refDocument);

        // TODO Méthode à tester lors de l'intégration

        RestTemplate rest = getRestTemplate();

        HttpHeaders headers = getRioRequestHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        String requestUrl = url + String.format(RIO_GET_FILE_DOCUMENT, codeAppli, refDocument, codeNotice, keyFile);
        URI uri = UriComponentsBuilder
                .fromHttpUrl(requestUrl)
                .queryParam("user", user)
                .build().encode().toUri();

        ResponseEntity<FileDocumentDTO> responseEntity = rest.exchange(uri, HttpMethod.GET, requestEntity, FileDocumentDTO.class);

        LOGGER.info("Fin récupération du fichier (keyfile) {} pour le document {}", keyFile, refDocument);

        return responseEntity.getBody();
    }

    private HttpHeaders getRioRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        headers.add("Authorization", "Bearer " + jwt);
        return headers;
    }

    private HttpHeaders getRioMultipartRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "multipart/form-data");
        headers.add("Accept", "*/*");
        headers.add("Authorization", "Bearer " + jwt);
        return headers;
    }

    private RestTemplate getRestTemplate() {
        RestTemplate rest = restTemplateBuilder.build();
        rest.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        rest.getMessageConverters().add(new FormHttpMessageConverter());
        return rest;
    }
}
