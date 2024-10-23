package mc.gouv.xaf.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
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

/**
 * Servlet servant à récupérer les brouillons d'un usager de façon paginée.
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/brouillonspage")
public class BrouillonsPageableController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrouillonsPageableController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @GetMapping
    public ResponseEntity<Page<BrouillonDTO>> doGet(@RequestParam(name = RequestConstant.PAGE_PARAM) String pageNb,
            @RequestParam(name = RequestConstant.SIZE_PARAM) String size,
            @RequestParam(name = RequestConstant.SORT_PARAM) String sort,
            @RequestParam(name = RequestConstant.DIRECTION_PARAM) String direction, HttpServletRequest request) {

        LOGGER.info("====================== /brouillonspage doGet()");

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

        try {
            LOGGER.info("Récupération des brouillons pour l'usager dont usagerId = {}", usagerId);
            Page<BrouillonDTO> page = getAfApiClient().getBrouillonsPageable(usagerId, paramDTO);
            return ResponseEntity.ok(page);
        } catch (Exception ex) {
            LOGGER.error("BrouillonsPageableServlet - Une erreur est survenue lors de l'appel à la méthode GET", ex);
            return ResponseEntity.status(getCodeErreur(ex)).build();
        }
    }
}
