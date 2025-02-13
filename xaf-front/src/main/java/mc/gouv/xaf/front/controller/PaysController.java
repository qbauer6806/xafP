package mc.gouv.xaf.front.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import mc.gouv.xaf.front.dto.NomenNomenclatureDTO;
import mc.gouv.xaf.front.dto.NomenValeurDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;

/**
 * Proxy vers les nomenclatures PAY-1 et NATIO de NOMEN
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/pays")
public class PaysController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaysController.class);

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    private List<NomenValeurDTO> listePays = new ArrayList<>();

    private Date listePaysLastUpdate = null;

    private List<NomenValeurDTO> listeNationalites = new ArrayList<>();

    private Date listeNationalitesLastUpdate = null;

    @GetMapping
    public ResponseEntity doGet(@RequestParam(required = false) String locale, HttpServletRequest request) {
        LOGGER.info("====================== /pays doGet()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        if (!listePays.isEmpty() && !isTimeToRefreshCache(listePaysLastUpdate)) {

            ObjectMapper objectMapper = new ObjectMapper();
            String valeursJson;
            try {
                valeursJson = objectMapper.writeValueAsString(listePays);

                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(valeursJson);
            } catch (JsonProcessingException e) {
                LOGGER.error("Erreur lors du objectMapper.writeValueAsString()", e);
            }
        }

        return getPays(locale);
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

        if (!listeNationalites.isEmpty() && !isTimeToRefreshCache(listeNationalitesLastUpdate)) {

            ObjectMapper objectMapper = new ObjectMapper();
            String valeursJson;
            try {
                valeursJson = objectMapper.writeValueAsString(listeNationalites);

                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(valeursJson);
            } catch (JsonProcessingException e) {
                LOGGER.error("Erreur lors du objectMapper.writeValueAsString()", e);
            }
        }

        return getNationalites();
    }

    private ResponseEntity getPays(String locale) {
        LOGGER.info("Refresh du cache Pays...");
        try {
            URI uri = new URIBuilder(propertiesResolver.getNomenUrl() + "/nomenclatures/PAY-1/valeurs")
                    .addParameter("valeurLangue", locale.toUpperCase()).build();
            LOGGER.debug("Appel à {}", uri);
            Request serviceRequest = Request.get(uri);
            serviceRequest.setHeader("Authorization", "Bearer " + propertiesResolver.getNomenJwt());
            try (ClassicHttpResponse serviceResponse = (ClassicHttpResponse) serviceRequest.execute()
                    .returnResponse()) {
                int statusCode = serviceResponse.getCode();
                String contentType = serviceResponse.getEntity().getContentType();

                if (statusCode == HttpStatus.OK.value()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    NomenNomenclatureDTO nomenNomenclatureDTO = objectMapper.readValue(
                            new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8),
                            NomenNomenclatureDTO.class);

                    List<NomenValeurDTO> valeurs = nomenNomenclatureDTO.getValeurs();

                    listePays = valeurs;
                    listePaysLastUpdate = new Date();

                    String valeursJson = objectMapper.writeValueAsString(valeurs);

                    return ResponseEntity.status(statusCode).header(HttpHeaders.CONTENT_TYPE, contentType)
                            .body(valeursJson);
                }
                LOGGER.info("====================== Fin /pays doGet()");

                return ResponseEntity.status(statusCode).build();
            }
        } catch (Exception e) {
            LOGGER.error("PaysServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.status(getCodeErreur(e)).build();
        }
    }

    private ResponseEntity getNationalites() {
        LOGGER.info("Refresh du cache Nationalites...");
        try {
            // Attention: la nomenclature NATIO ne connait que la langue française !
            URI uri = new URIBuilder(propertiesResolver.getNomenUrl() + "/nomenclatures/NATIO/valeurs")
                    .addParameter("valeurLangue", "FR").build();
            LOGGER.debug("Appel à {}", uri);
            Request serviceRequest = Request.get(uri);
            serviceRequest.setHeader("Authorization", "Bearer " + propertiesResolver.getNomenJwt());
            try (ClassicHttpResponse serviceResponse = (ClassicHttpResponse) serviceRequest.execute()
                    .returnResponse()) {
                int statusCode = serviceResponse.getCode();
                String contentType = serviceResponse.getEntity().getContentType();

                if (statusCode == HttpStatus.OK.value()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    NomenNomenclatureDTO nomenNomenclatureDTO = objectMapper.readValue(
                            new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8),
                            NomenNomenclatureDTO.class);

                    List<NomenValeurDTO> valeurs = nomenNomenclatureDTO.getValeurs();

                    listeNationalites = valeurs;
                    listeNationalitesLastUpdate = new Date();

                    String valeursJson = objectMapper.writeValueAsString(valeurs);

                    return ResponseEntity.status(statusCode).header(HttpHeaders.CONTENT_TYPE, contentType)
                            .body(valeursJson);
                }
                LOGGER.info("====================== Fin /pays doGet()");

                return ResponseEntity.status(statusCode).build();
            }
        } catch (Exception e) {
            LOGGER.error("PaysServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.status(getCodeErreur(e)).build();
        }
    }

    private boolean isTimeToRefreshCache(Date lastUpdate) {
        if (listePaysLastUpdate == null) {
            return true;
        }
        return new Date()
                .getTime() > (lastUpdate.getTime() + Integer.parseInt(propertiesResolver.getPaysCacheDuration()));
    }

}
