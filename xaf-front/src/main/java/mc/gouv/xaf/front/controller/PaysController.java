package mc.gouv.xaf.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Proxy vers le référentiel Pays
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/pays")
public class PaysController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaysController.class);

    public static final String NATIONALITE_PATH = "/nationalites";

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @GetMapping
    public ResponseEntity doGet(@RequestParam(required = false) String locale, HttpServletRequest request) {
        LOGGER.info("====================== /pays doGet()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        try {
            URI uri = new URIBuilder(propertiesResolver.getPaysUrl()).addParameter("locale", locale).build();
            LOGGER.info("Appel à {}", uri);
            Request serviceRequest = Request.get(uri);
            serviceRequest.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.getType());
            ClassicHttpResponse serviceResponse = (ClassicHttpResponse)serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getCode();

            if (statusCode == HttpStatus.OK.value()) {
                return ResponseEntity.status(statusCode)
                        .contentType(MediaType.valueOf(serviceResponse.getEntity().getContentType()))
                        .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
            }
            LOGGER.info("====================== Fin /pays doGet()");

            return ResponseEntity.status(statusCode).build();
        } catch (Exception e) {
            LOGGER.error("PaysServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.status(getCodeErreur(e)).build();
        }
    }


    @GetMapping("/nationalites")
    public ResponseEntity doGetNationalites(@RequestParam(required = false) String locale, HttpServletRequest request) {
        LOGGER.info("====================== /pays/nationalités doGet()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        try {
            URI uri = new URIBuilder(propertiesResolver.getPaysUrl() + NATIONALITE_PATH).addParameter("locale", locale).build();
            LOGGER.info("Appel à {}", uri);
            Request serviceRequest = Request.get(uri);
            serviceRequest.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.getType());
            ClassicHttpResponse serviceResponse = (ClassicHttpResponse)serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getCode();

            if (statusCode == HttpStatus.OK.value()) {
                return ResponseEntity.status(statusCode)
                        .contentType(MediaType.valueOf(serviceResponse.getEntity().getContentType()))
                        .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
            }
            LOGGER.info("====================== Fin /pays doGet()");

            return ResponseEntity.status(statusCode).build();
        } catch (Exception e) {
            LOGGER.error("PaysServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.status(getCodeErreur(e)).build();
        }
    }

}
