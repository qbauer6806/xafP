package mc.gouv.xaf.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/demandespage")
public class DemandesPageableController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesPageableController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @GetMapping
    public ResponseEntity<Page<DemandeDTO>> doGet(@RequestParam(name = RequestConstant.PAGE_PARAM) String pageNb,
            @RequestParam(name = RequestConstant.SIZE_PARAM) String size,
            @RequestParam(name = RequestConstant.SORT_PARAM) String sort,
            @RequestParam(name = RequestConstant.DIRECTION_PARAM) String direction,
            @RequestParam(name = RequestConstant.STATUS_PARAM) String status,
            @RequestParam(name = RequestConstant.LANG_PARAM) String lang, HttpServletRequest request) {

        LOGGER.info("====================== /demandespage doGet()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();

        // Récupération des paramètres
        PageParamDTO paramDTO = new PageParamDTO();
        try {
            if (StringUtils.isNotBlank(pageNb)) {
                paramDTO.setPage(Integer.parseInt(pageNb));
            }
            if (StringUtils.isNotBlank(size)) {
                paramDTO.setSize(Integer.parseInt(size));
            }
        } catch (NumberFormatException e) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Problème lors du parsing des paramètres");
        }

        if (StringUtils.isNotBlank(sort)) {
            paramDTO.setSort(sort);
        }
        if (StringUtils.isNotBlank(direction)) {
            paramDTO.setDirection(direction);
        }
        if (StringUtils.isNotBlank(status)) {
            paramDTO.setStatus(status);
        }
        if (StringUtils.isNotBlank(lang)) {
            paramDTO.setLang(lang);
        }

        try {
            LOGGER.info("Récupération des demandes pour l'usager dont usagerId = {}", usagerId);
            Page<DemandeDTO> page = getAfApiClient().getDemandesPageable(usagerId, paramDTO);

            LOGGER.info("====================== FIN /demandespage doGet()");

            return ResponseEntity.ok(page);
        } catch (Exception ex) {
            LOGGER.error("DemandesPageableServlet - Une erreur est survenue lors de l'appel à la méthode GET", ex);
            return ResponseEntity.status(getCodeErreur(ex)).build();
        }

    }
}
