package mc.gouv.xaf.front.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.front.dto.CustomRequestRechercheDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.enums.HttpMethod;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Servlet mettant à disposition le service /customRequest avec les méthodes PUT, POST, GET, DELETE.
 * Cette servlet permet d'appeler des fonctions API custom/spécifiques d'une démarche
 *
 * @author qdeme
 */
@RestController
@RequestMapping("/customRequest")
public class CustomRequestController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRequestController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    // List des headers qui sont interdits à copier, voir https://hg.openjdk.org/jdk8u/jdk8u-dev/jdk/file/31bc1a681b51/src/share/classes/sun/net/www/protocol/http/HttpURLConnection.java#l186
    private final String[] restrictedHeaders = {
            "Access-Control-Request-Headers",
            "Access-Control-Request-Method",
            "Connection", /* close is allowed */
            "Content-Length",
            "Content-Transfer-Encoding",
            "Host",
            "Keep-Alive",
            "Origin",
            "Trailer",
            "Transfer-Encoding",
            "Upgrade",
            "Via"
    };

    public ResponseEntity doHttpMethod(HttpServletRequest request, HttpMethod httpMethod) {

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();
        LOGGER.info("UsagerID={}", usagerId);

        String pathInfo = request.getPathInfo();
        String restOfUrl = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            restOfUrl = "/" + pathInfo.split("/")[1];
        }

        String serviceUrl = propertiesResolver.getApiUrl() + "/customRequest";

        if (StringUtils.isNotBlank(restOfUrl)) {
            serviceUrl += restOfUrl;
        }

        serviceUrl += "?";

        if (StringUtils.isNotBlank(request.getQueryString())) {
        	serviceUrl += "?" + request.getQueryString() + "&usagerId="+usagerId;
        }else {
            serviceUrl += "?usagerId="+usagerId;
        }

        if (request.getParameter("usagerId") == null) {
            serviceUrl += "&usagerId=" + usagerInfosDTO.getId();
        }

        LOGGER.info("Appel à {}", serviceUrl);

        Request serviceRequest = this.getRequest(request, httpMethod, serviceUrl);
        if (serviceRequest == null) {
            //Les logs sont gérés dans la méthode getRequest. On ne fait rien
            LOGGER.error("Situation anormale : serviceRequest == null");
            return ResponseEntity.internalServerError().build();
        }
        serviceRequest.setHeader("Authorization", "Bearer " + propertiesResolver.getApiJwt());

        // Copier les headers
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String elem = headers.nextElement();
            if (!Arrays.asList(restrictedHeaders).contains(elem)) {
                serviceRequest.setHeader(elem, request.getHeader(elem));
            }
        }
        try {
            HttpResponse serviceResponse = serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getStatusLine().getStatusCode();
            return ResponseEntity.status(statusCode)
                    .contentType(MediaType.valueOf(serviceResponse.getEntity().getContentType().getValue()))
                    .body(serviceResponse.getEntity().getContent());
        } catch (Exception e) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du traitement de la réponse");
        }
    }

    private Request getRequest(HttpServletRequest request, HttpMethod httpMethod, String serviceUrl) {
        Request serviceRequest = null;

        try {
            if (HttpMethod.GET.equals(httpMethod)) {
                serviceRequest = Request.Get(serviceUrl);
            } else if (HttpMethod.POST.equals(httpMethod)) {
                serviceRequest = Request.Post(serviceUrl);
                serviceRequest.bodyByteArray(IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8).getBytes());
            } else if (HttpMethod.PUT.equals(httpMethod)) {
                serviceRequest = Request.Put(serviceUrl);
                serviceRequest.bodyByteArray(IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8).getBytes());
            } else if (HttpMethod.DELETE.equals(httpMethod)) {
                serviceRequest = Request.Delete(serviceUrl);
            }
        } catch (IOException e) {
            return null;
        }
        return serviceRequest;
    }

    private Request getRequest(HttpServletRequest request, String serviceUrl) throws IOException {
        String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        try {
            CustomRequestRechercheDTO rechercheInput = mapper.readValue(body, CustomRequestRechercheDTO.class);
            if (rechercheInput.getData() != null) {
                String sComplementPost = String.format("&numeroContrat=%s&numeroFacture=%s&numeroTiers=%s",
                        URLEncoder.encode(rechercheInput.getData().getNumeroContrat(), StandardCharsets.UTF_8),
                        URLEncoder.encode(rechercheInput.getData().getNumeroFacture(), StandardCharsets.UTF_8),
                        URLEncoder.encode(rechercheInput.getData().getNumeroTiers(), StandardCharsets.UTF_8));
                serviceUrl += sComplementPost;
            }
        } catch (Exception e) {
            LOGGER.info("Exception lors de la deserialization de CustomRequestRechercheDTO", e);
        }
        Request serviceRequest = Request.Post(serviceUrl);
        serviceRequest.bodyByteArray(body.getBytes());

        return serviceRequest;
    }

    @PostMapping
    public ResponseEntity doPost(HttpServletRequest request) {
        LOGGER.info("====================== /customRequest doPost()");
        return doHttpMethod(request, HttpMethod.POST);
    }

    @PutMapping
    public ResponseEntity doPut(HttpServletRequest request) {
        LOGGER.info("====================== /customRequest doPut()");
        return doHttpMethod(request, HttpMethod.PUT);
    }

    @GetMapping
    public ResponseEntity doGet(HttpServletRequest request) {
        LOGGER.info("====================== /customRequest doGet()");
        return doHttpMethod(request, HttpMethod.GET);
    }

    @DeleteMapping
    public ResponseEntity doDelete(HttpServletRequest request) {
        LOGGER.info("====================== /demandes doDelete()");
        return doHttpMethod(request, HttpMethod.DELETE);
    }
}
