package mc.gouv.xaf.front.controller;

import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.enums.HttpMethod;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
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

        if (StringUtils.isNotBlank(request.getQueryString())) {
            serviceUrl += "?" + request.getQueryString();
        }
        LOGGER.info("Appel à {}", serviceUrl);

        Request serviceRequest = null;

        try {
            if (HttpMethod.GET.equals(httpMethod)) {
                serviceRequest = Request.Get(serviceUrl);
            } else if (HttpMethod.POST.equals(httpMethod)) {
                serviceRequest = Request.Post(serviceUrl);
                serviceRequest.bodyByteArray(IOUtils.toString(request.getInputStream()).getBytes());
            } else if (HttpMethod.PUT.equals(httpMethod)) {
                serviceRequest = Request.Put(serviceUrl);
                serviceRequest.bodyByteArray(IOUtils.toString(request.getInputStream()).getBytes());
            } else if (HttpMethod.DELETE.equals(httpMethod)) {
                serviceRequest = Request.Delete(serviceUrl);
            }
        } catch (IOException e) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    "CustomRequestServlet - Une erreur est survenue lors de l'appel à la méthode " + httpMethod.name());
        }

        if (serviceRequest == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Situation anormale : serviceRequest == null");
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

            LOGGER.info("====================== Fin /customRequest doMethod()");

            return ResponseEntity.status(statusCode).contentType(MediaType.valueOf(serviceResponse.getEntity().getContentType().getValue()))
                    .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du traitement de la réponse");
        }
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
