package mc.gouv.xaf.backweb.controller;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import mc.gouv.xaf.back.shared.dto.JobNamesEnum;

@Controller
@RequestMapping("/gestion/jobs")
@Secured("ROLE_CONFIGURATION")
public class GestionJobsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionJobsController.class);

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView form() throws Exception {

        LOGGER.info("Appel de la page gestion/jobs. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/jobs/gestionjobs");
        mav.addObject("jobs", Arrays.asList(JobNamesEnum.values()));

        LOGGER.info("======================= Fin gestion/jobs. Méthode form");

        return mav;
    }

}
