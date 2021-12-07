package mc.gouv.xaf.backweb.controller;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.TemplatesService;
import mc.gouv.xaf.back.service.templates.GestionTemplateService;
import mc.gouv.xaf.shared.formbean.TemplateFormBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller la modification de templates
 * 
 * @author mpavone
 * 
 */
@Controller
@Secured({"ROLE_PARAMETRAGE", "ROLE_CONFIGURATION"})
@RequestMapping("/gestion/template")
public class GestionTemplateController extends AbstractController {

    @Autowired
    private GestionTemplateService gestionTemplateService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private TemplatesService templatesService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionParametresController.class);

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView getTemplates() {

        LOGGER.info("Appel de la page /gestion/template. Méthode getTemplates");
        ModelAndView mav = new ModelAndView("gestion/template/template");

        mav.addObject("tsName", gouvPropertiesResolver.getDemarcheId());
        mav.addObject("templateList", templatesService.getTemplates(gouvPropertiesResolver.getDemarcheId()));

        LOGGER.info("======================= Fin /gestion/template. Méthode getTemplates");

        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/update")
    public ModelAndView formUpdateInit(@ModelAttribute("templateFormBean") TemplateFormBean templateFormBean) {

        LOGGER.info("Appel de la page /gestion/template/update. Méthode formUpdateInit");
        ModelAndView mav = new ModelAndView("gestion/template/templateupdate");

        mav.addObject("tsName", gouvPropertiesResolver.getDemarcheId());

        gestionTemplateService.retrieveTemplateForm(templateFormBean);

        LOGGER.info("======================= Fin /gestion/template/update. Méthode formUpdateInit");

        return mav;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/update")
    public ModelAndView traiterUpdate(@ModelAttribute("templateFormBean") TemplateFormBean templateFormBean) {

        LOGGER.info("Appel de la page /gestion/template/update. Méthode traiterUpdate");
        ModelAndView mav = new ModelAndView("gestion/template/templateupdate");

        mav.addObject("tsName", gouvPropertiesResolver.getDemarcheId());

        gestionTemplateService.saveTemplateForm(templateFormBean);

        // Récupération à nouveau du template pour vérifier que tout est ok
        gestionTemplateService.retrieveTemplateForm(templateFormBean);

        LOGGER.info("======================= Fin /gestion/template/update. Méthode traiterUpdate");

        return mav;
    }
}
