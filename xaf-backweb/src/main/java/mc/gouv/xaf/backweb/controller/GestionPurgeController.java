package mc.gouv.xaf.backweb.controller;

import mc.gouv.xaf.back.service.purge.PurgeDemandesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controller pour la page /purge
 * 
 * @author mpavone.Ext
 *
 */
@Controller
@RequestMapping("/gestion/purge")
@Secured("ROLE_PARAMETRAGE")
public class GestionPurgeController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionPurgeController.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PurgeDemandesService purgeService;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView form() {

        LOGGER.info("======================= Appel de la page /gestion/purge");

        ModelAndView mav = new ModelAndView("gestion/purge/gestionpurge");

        mav.addObject("demandesPurgees", purgeService.getDemandesPurgees());

        LOGGER.info("======================= Fin /gestion/purge");

        return mav;
    }
}
