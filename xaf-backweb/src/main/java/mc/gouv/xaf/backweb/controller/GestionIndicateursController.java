package mc.gouv.xaf.backweb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller pour les fonctionnalites (onglets) Indicateurs
 *
 * @author tverdoyan
 */
@Controller
@RequestMapping("/gestion/indicateurs")
public class GestionIndicateursController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionIndicateursController.class);

    @Secured("ROLE_EXPORT")
    @GetMapping
    public ModelAndView formUser() {
        LOGGER.info("Appel de la page /gestion/indicateurs. Méthode formUser");
        ModelAndView mav = new ModelAndView("gestion/indicateurs/indicateurs");
        LOGGER.info("======================= Fin /gestion/indicateurs. Méthode formUser");
        return mav;
    }
}
