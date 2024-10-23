package mc.gouv.xaf.backweb.controller;

import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.StatistiquesInternesService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequestMapping("/gestion/statistiques")
@Secured({ "ROLE_CONFIGURATION", "ROLE_PARAMETRAGE" })
public class GestionStatistiquesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionStatistiquesController.class);

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private StatistiquesInternesService statistiquesService;

    @Autowired
    private BrouillonsService brouillonsService;

    @GetMapping
    public ModelAndView form() {
        LOGGER.info("Appel de la page gestion/statistiques. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/stats/statistiques");
        LOGGER.info("Récupération des statistiques par statut de démarche");
        Map<String, Map<String, Long>> statListstatList = statistiquesService.getNumberOfEachDemandes();
        mav.addObject("statList", statListstatList);
        mav.addObject("canalEnum", DemandeCanalEnum.values());
        mav.addObject("statusEnum", demarchesDataProvider.getStatusMap());
        mav.addObject("privateStatusEnum", demarchesDataProvider.getPrivateStatusMap());
        mav.addObject("brouillonsNbr", brouillonsService.getNombreBrouillons());
        LOGGER.info("======================= Fin /gestion/statistiques. Méthode form");
        return mav;
    }

}
