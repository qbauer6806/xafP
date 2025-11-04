package mc.gouv.xaf.backweb.controller;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.purge.PurgeDemandesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller pour la page /purge
 *
 * @author mpavone.Ext
 */
@Controller
@RequestMapping("/gestion/purge")
@Secured("ROLE_PARAMETRAGE")
@RequiredArgsConstructor
public class GestionPurgeController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionPurgeController.class);

    private final PurgeDemandesService purgeService;
    private final DemarchesDataProvider demarchesDataProvider;

    @GetMapping()
    public ModelAndView form() {
        LOGGER.info("======================= Appel de la page /gestion/purge");
        ModelAndView mav = new ModelAndView("gestion/purge/gestionpurge");
        mav.addObject("demandesPurgees", purgeService.getDemandesPurgees());
        mav.addObject("statutsAPurger", demarchesDataProvider.getStatutsAPurger());
        mav.addObject("derniereExec", purgeService.getDateDerniereExecution());
        mav.addObject("statutsMap", demarchesDataProvider.getStatusMap());
        LOGGER.info("======================= Fin /gestion/purge");
        return mav;
    }
}
