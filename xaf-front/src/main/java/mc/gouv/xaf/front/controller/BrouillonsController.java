package mc.gouv.xaf.front.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.enums.HttpMethod;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SessionConstant;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * Servlet mettant à disposition le service /brouillons avec les méthodes PUT, POST, GET, DELETE. Cette servlet récupère
 * le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS correspondants dans le back-end générique.
 *
 * @author qdeme
 */
@Controller
@RequiredArgsConstructor
public class BrouillonsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrouillonsController.class);

    private final XafFrontserverUtils xafFrontserverUtils;

    /**
     * Factorisation des méthodes PUT et POST
     *
     * @param request
     *         Requête initiale de la Servlet
     * @param httpMethod
     *         Indique si l'on souhaite effectuer un POST ou un PUT
     */
    private ResponseEntity doHttpMethod(String brouillonId, HttpServletRequest request, HttpMethod httpMethod) {

        if (!HttpMethod.PUT.equals(httpMethod) && !HttpMethod.POST.equals(httpMethod)) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Mauvais appel de méthode.");
        }

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        AfApiClient afApiClient = xafFrontserverUtils.getAfApiClient();

        if (usagerInfosDTO == null) {
            return ResponseEntity.internalServerError().build();
        }

        // Récupération du JSON reçu en input et transmission au 2ème service en UTF8
        StringBuilder buffer = new StringBuilder();
        try {
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.toString().isEmpty()) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST, "Erreur: JSON manquant");
            }

            LOGGER.info("Appel à la démarche pour créer le brouillon");
            ObjectMapper mapper = new ObjectMapper();
            BrouillonDTO brouillonInput = mapper.readValue(buffer.toString(), BrouillonDTO.class);

            ResponseEntity.BodyBuilder response;
            BrouillonDTO brouillonDto;

            if (request.getSession().getAttribute(SessionConstant.SESSION_DEMANDE_INITIALE) != null) {
                brouillonInput.setContenuInitial(mapper.valueToTree(
                        request.getSession().getAttribute(SessionConstant.SESSION_DEMANDE_INITIALE)));
            }
            if (HttpMethod.POST.equals(httpMethod)) {
                brouillonDto = afApiClient.creerBrouillon(brouillonInput, usagerInfosDTO.getId());
                response = ResponseEntity.status(HttpStatus.CREATED);
            } else {
                brouillonDto = afApiClient.updateBrouillon(brouillonInput, Integer.parseInt(brouillonId),
                        usagerInfosDTO.getId());
                response = ResponseEntity.status(HttpStatus.OK);
            }

            return response.body(brouillonDto);
        } catch (IOException | NumberFormatException e) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    "BrouillonsServlet - Une erreur est survenue lors de l'appel à la méthode " + httpMethod.name());
        }

    }

    @PostMapping(value = { "/brouillons", "/brouillons/{brouillonId}" })
    public ResponseEntity doPost(@PathVariable(required = false) String brouillonId, HttpServletRequest request) {
        LOGGER.info("====================== /brouillons doPost()");
        return doHttpMethod(brouillonId, request, HttpMethod.POST);
    }

    @PutMapping(value = { "/brouillons", "/brouillons/{brouillonId}" })
    public ResponseEntity doPut(@PathVariable(required = false) String brouillonId, HttpServletRequest request) {
        LOGGER.info("====================== /brouillons doPut()");
        return doHttpMethod(brouillonId, request, HttpMethod.PUT);
    }

    @GetMapping(value = { "/brouillons", "/brouillons/{brouillonId}" })
    public ResponseEntity doGet(@PathVariable String brouillonId, HttpServletRequest request) {
        LOGGER.info("====================== /brouillons doGet()");

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        AfApiClient afApiClient = xafFrontserverUtils.getAfApiClient();

        if (usagerInfosDTO == null) {
            return ResponseEntity.internalServerError().build();
        }

        try {
            LOGGER.info("Appel à la démarche pour récupérer le brouillon {}", XafFrontserverUtils.logSafe(brouillonId));
            BrouillonDTO brouillonDto = afApiClient.getBrouillon(Integer.parseInt(brouillonId), usagerInfosDTO.getId());

            if (brouillonDto.getContenuInitial() != null) {
                request.getSession()
                        .setAttribute(SessionConstant.SESSION_DEMANDE_INITIALE, brouillonDto.getContenuInitial());
            }
            return ResponseEntity.ok(brouillonDto);
        } catch (Exception e) {
            LOGGER.error("BrouillonsServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping(value = { "/brouillons", "/brouillons/{brouillonId}" })
    public ResponseEntity doDelete(@PathVariable(required = false) String brouillonId, HttpServletRequest request) {
        LOGGER.info("====================== /brouillons doDelete()");

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        AfApiClient afApiClient = xafFrontserverUtils.getAfApiClient();

        if (usagerInfosDTO == null) {
            return ResponseEntity.internalServerError().build();
        }

        LOGGER.info("Appel à la démarche pour supprimer le brouillon");
        try {
            afApiClient.deleteBrouillon(Integer.parseInt(brouillonId), usagerInfosDTO.getId());
            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            LOGGER.error("BrouillonsServlet - Une erreur est survenue lors de l'appel à la méthode DELETE", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
