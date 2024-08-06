package mc.gouv.xaf.front.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import mc.gouv.xaf.front.dto.DocHolderFileSearchDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doc-holder/search")
public class DocHolderSearchController extends AbstractXafController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderSearchController.class);
    private static final String SERVICE_URL = "/search";


    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver frontGouvPropertiesResolver;

    /**
     * Méthode pour l'opération <b>searchFiles</b>
     * Elle permet de récupérer la liste de tous les documents enregistrés dans le porte-document de l'utilisateur connecté
     */
    @PostMapping
    protected ResponseEntity doPost(HttpServletRequest req) throws IOException {
        LOGGER.info("====================== {} doPost()", req.getServletPath());

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        LOGGER.info("Vérification usager connecté");
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        DocHolderFileSearchDTO fileSearchDTO;

        try {
            fileSearchDTO = mapper.readValue(req.getInputStream(), DocHolderFileSearchDTO.class);
        } catch (IOException ioe) {
            LOGGER.error("Impossible de déserialiser la requête", ioe);
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
        }

        // Si aucun opérateur n'est donné, on utilise AND par défaut
        if (fileSearchDTO.getOperator() == null) {
            fileSearchDTO.setOperator(DocHolderFileSearchDTO.OperatorEnum.AND);
        }

        Request serviceRequest = Request.post(frontGouvPropertiesResolver.getPorteDocUrl() + SERVICE_URL);
        serviceRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            serviceRequest.bodyString(mapper.writeValueAsString(fileSearchDTO), ContentType.APPLICATION_JSON);
            ClassicHttpResponse serviceResponse = (ClassicHttpResponse)serviceRequest.execute().returnResponse();

            LOGGER.info("====================== Fin {} doPost()", req.getServletPath());
            return ResponseEntity.status(serviceResponse.getCode())
                    .contentType(MediaType.valueOf(serviceResponse.getEntity().getContentType()))
                    .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents searchFiles", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
