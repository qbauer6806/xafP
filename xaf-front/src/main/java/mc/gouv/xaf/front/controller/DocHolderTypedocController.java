package mc.gouv.xaf.front.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doc-holder/typedoc")
public class DocHolderTypedocController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderTypedocController.class);
    private static final String SERVICE_URL = "/typedoc";

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver frontGouvPropertiesResolver;

    /**
     * Méthode pour l'opération <b>getDocTypedoc</b> Elle permet de récupérer toutes les catégories de documents
     * disponibles pour le porte-documents
     */
    @GetMapping
    protected ResponseEntity doGet(HttpServletRequest req) throws ServletException, IOException {
        LOGGER.info("====================== {} doGet()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        Request serviceRequest = Request.get(frontGouvPropertiesResolver.getPorteDocUrl() + SERVICE_URL);
        serviceRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

        try {
            ClassicHttpResponse serviceResponse = (ClassicHttpResponse) serviceRequest.execute().returnResponse();
            int statusCode = serviceResponse.getCode();

            LOGGER.info("====================== Fin {} doGet()", req.getServletPath());
            return ResponseEntity.status(statusCode)
                    .contentType(MediaType.valueOf(serviceResponse.getEntity().getContentType()))
                    .body(new String(serviceResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
        } catch (ClientProtocolException e) {
            LOGGER.error("Erreur lors de l'exécution de l'appel à monguichet, erreur protocole HTTP", e);
            return ResponseEntity.internalServerError().build();
        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error("Erreur lors de l'appel à l'API Porte-Documents getTypedoc", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
