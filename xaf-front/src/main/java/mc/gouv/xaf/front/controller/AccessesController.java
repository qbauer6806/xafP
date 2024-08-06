package mc.gouv.xaf.front.controller;

import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import mc.gouv.xapi.error.exception.client.NotFoundWebException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet mettant à disposition le service /accesses avec les méthodes PUT, POST, GET, DELETE. Cette servlet récupère
 * le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS correspontants dans le back-end générique.
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/accesses")
public class AccessesController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessesController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    /**
     * Traitement des méthodes POST
     *
     * @param request Requête initiale de la Servlet
     */
    @PostMapping
    public ResponseEntity<AccessDTO> doPost(@RequestBody AccessInputDTO accessInput, HttpServletRequest request) {
        LOGGER.info("====================== /accesses doPost()");
        UsagerInfosDTO usagerInfosDTO = getUsagerId(request);
        if (null == usagerInfosDTO) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        LOGGER.info("Appel à la démarche pour créer l'accès...");
        AccessDTO access = getAfApiClient().createOrUpdateAccess(usagerInfosDTO.getId(), accessInput);

        LOGGER.info("Incorporer l'AccessID dans la session pour protéger les appels à FILE... accessId={}", access.getPkAccess());
        usagerInfosDTO.setAccessId(access.getPkAccess());
        request.getSession().setAttribute("login", usagerInfosDTO);

        LOGGER.info("====================== Fin /accesses doPost()");

        return ResponseEntity.ok(access);
    }

    /**
     * Traitement des méthodes GET
     *
     * @param request Requête initiale de la Servlet
     */
    @GetMapping
    public ResponseEntity doGet(HttpServletRequest request) {
        LOGGER.info("====================== /accesses doGet()");
        UsagerInfosDTO usagerInfosDTO = getUsagerId(request);
        if (null == usagerInfosDTO) {
            return ResponseEntity.badRequest().build();
        }

        LOGGER.info("Appel à la démarche pour récupérer l'accès...");
        AccessDTO access;
        try {
            access = getAfApiClient().getAccess(usagerInfosDTO.getId());
        } catch (NotFoundWebException e) {
            return ResponseEntity.notFound().build();
        }
        LOGGER.info("Incorporer l'AccessID dans la session pour protéger les appels à FILE... accessId={}", access.getPkAccess());
        HttpSession session = request.getSession();
        usagerInfosDTO.setAccessId(access.getPkAccess());
        session.setAttribute("login", usagerInfosDTO);

        LOGGER.info("====================== Fin /accesses doGet()");

        return ResponseEntity.ok(access);
    }

    /**
     * Traitement des méthodes DELETE
     *
     * @param request Requête initiale de la Servlet
     */
    @DeleteMapping
    public ResponseEntity doDelete(HttpServletRequest request) {
        LOGGER.info("====================== /accesses doDelete()");
        UsagerInfosDTO usagerInfosDTO = getUsagerId(request);
        if (null == usagerInfosDTO) {
            return ResponseEntity.badRequest().build();
        }

        LOGGER.info("Appel de la démarche pour désinscrire l'usager...");
        String langue = request.getParameter("langue");
        getAfApiClient().desinscriptionUsager(usagerInfosDTO.getId(), langue);

        LOGGER.info("Inclure la réponse dans le HttpServletResponse...");
        LOGGER.info("====================== Fin /accesses doDelete()");
        return ResponseEntity.ok().build();
    }


    private UsagerInfosDTO getUsagerId(HttpServletRequest request) {
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return null;
        }
        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();
        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = propertiesResolver.getDemarcheId();
        LOGGER.info("DemarcheID={}, UsagerID={}", demarcheId, usagerId);
        return usagerInfosDTO;
    }
}
