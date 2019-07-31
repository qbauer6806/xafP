package mc.gouv.af.backweb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/gestion/adminrecherche")
@Secured("ROLE_CONFIGURATION")
public class GestionAdminRechercheController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionAdminRechercheController.class);

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView form() throws Exception {

        LOGGER.info("Appel de la page gestion/adminrecherche. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/adminrecherche/adminrecherche");

        LOGGER.info("======================= Fin /gestion/adminrecherche. Méthode form");

        return mav;
    }

}
