package mc.gouv.xaf.front.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;

@Controller
@RequestMapping("/api2tiers")
public class Proxy2TiersController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxy2TiersController.class);
    
    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity doHttpMethod(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = false) MultipartFile data) {
        LOGGER.info("====================== /api2tiers doHttpMethod({})", request.getMethod());
        // Récupérer l'URL de la requête entrante
        String requestUrl = request.getRequestURL().toString();
        
        String queryString = request.getQueryString(); // Récupérer les paramètres de la requête
        
        // Décoder les paramètres de la requête
        if (queryString != null) {
         	queryString = URLDecoder.decode(queryString, StandardCharsets.UTF_8);
        }
        
        LOGGER.info("requestUrl={}", requestUrl);
        
        RestTemplate restTemplate = new RestTemplate();
        
        String apiUrl = propertiesResolver.getApiUrl().replace("/api/v1", "") + extractApiUrl(requestUrl, "/api2tiers", queryString);
        
        LOGGER.info("apiUrl={}", apiUrl);

        // Récupérer les headers de la requête entrante
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            
            // Remplacer le header Authorization donné par le client appelant le FO par le nôtre pour appeler l'API
            if ("Authorization".equals(headerName)) {
            	headers.add(headerName, "Bearer " + propertiesResolver.getApiJwt());
            }
            else {
            	headers.add(headerName, request.getHeader(headerName));
            }
        }
        
        try {
            // Effectuer la requête sortante
            ResponseEntity<byte[]> responseEntity = null;
            if (request.getMethod().equals("GET") || request.getMethod().equals("DELETE")) {
                responseEntity = restTemplate.exchange(apiUrl, HttpMethod.valueOf(request.getMethod()), new HttpEntity<>(headers), byte[].class);
            } else {
                // Vérifier s'il y a un fichier joint
                if (data != null && !data.isEmpty()) {
                    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                    body.add("data", data.getResource());

                    // Ajouter les autres paramètres de la requête, s'il y en a
                    Enumeration<String> parameterNames = request.getParameterNames();
                    while (parameterNames.hasMoreElements()) {
                        String paramName = parameterNames.nextElement();
                        body.add(paramName, request.getParameter(paramName));
                    }

                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                    responseEntity = restTemplate.exchange(apiUrl, HttpMethod.valueOf(request.getMethod()), requestEntity, byte[].class);
                } else {
                    // Si aucun fichier n'est joint, traiter le corps de la requête comme d'habitude
                    InputStream requestBodyStream = request.getInputStream();
                    // Copier le corps de la requête entrante dans un ByteArrayOutputStream
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = requestBodyStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                    byte[] requestBody = outputStream.toByteArray();

                    responseEntity = restTemplate.exchange(apiUrl, HttpMethod.valueOf(request.getMethod()), new HttpEntity<>(requestBody, headers), byte[].class);
                }
            }

            return ResponseEntity.status(responseEntity.getStatusCode())
                    .headers(responseEntity.getHeaders())
                    .body(responseEntity.getBody());
	    } catch (HttpClientErrorException ex) {
	        return ResponseEntity.status(ex.getRawStatusCode())
	            .headers(ex.getResponseHeaders())
	            .body(ex.getResponseBodyAsString());
	    } catch (IOException ex) {
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }
    
    private String extractApiUrl(String requestUrl, String keyword, String queryString) {
        try {
            URI uri = new URI(requestUrl);
            String path = uri.getPath();
            String apiPath = path.substring(path.indexOf(keyword));
            if (queryString != null && !queryString.isEmpty()) {
                apiPath += "?" + queryString; // Ajouter les paramètres de requête à l'URL
            }
            return apiPath;
        } catch (Exception e) {
            e.printStackTrace(); // Gérer l'erreur selon vos besoins
        }
        return requestUrl; // Retourne l'URL originale si la transformation échoue
    }
}
