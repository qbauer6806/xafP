package mc.gouv.xaf.backweb.controller;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.TemplatesService;
import mc.gouv.xaf.back.service.templates.GestionTemplateService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.shared.formbean.TemplateFormBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller la modification de templates
 * 
 * @author mpavone
 * 
 */
@Controller
@Secured({"ROLE_CONFIGURATION"})
@RequestMapping("/gestion/template")
public class GestionTemplateController extends AbstractController {

    @Autowired
    private GestionTemplateService gestionTemplateService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private TemplatesService templatesService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionTemplateController.class);
    private static final String TS_CODE_VAR = "tsCode";

    // Messages
    private static final String MESSAGE_SUCCESS_MODIFICATION = "Le template mail a été modifié avec succès";

    @GetMapping
    public ModelAndView getTemplates() {

        LOGGER.info("Appel de la page /gestion/template. Méthode getTemplates");
        ModelAndView mav = new ModelAndView("gestion/template/template");

        boolean frOnly = isFrenchOnly();

        mav.addObject(TS_CODE_VAR, gouvPropertiesResolver.getDemarcheId());
        mav.addObject("frOnly", frOnly);

        List<TemplateDTO> templateList = frOnly ? templatesService.getTemplates(gouvPropertiesResolver.getDemarcheId(), "fr") :
                templatesService.getTemplates(gouvPropertiesResolver.getDemarcheId());

        mav.addObject("templateList", templateList);

        LOGGER.info("======================= Fin /gestion/template. Méthode getTemplates");

        return mav;
    }

    @GetMapping(path = "/update")
    public ModelAndView formUpdateInit(@ModelAttribute("templateFormBean") TemplateFormBean templateFormBean) {

        LOGGER.info("Appel de la page /gestion/template/update. Méthode formUpdateInit");
        ModelAndView mav = new ModelAndView("gestion/template/templateupdate");

        mav.addObject(TS_CODE_VAR, gouvPropertiesResolver.getDemarcheId());
        mav.addObject("frOnly", isFrenchOnly());

        gestionTemplateService.retrieveTemplateForm(templateFormBean);

        LOGGER.info("======================= Fin /gestion/template/update. Méthode formUpdateInit");

        return mav;
    }

    @PostMapping(path = "/update")
    public ModelAndView traiterUpdate(@ModelAttribute("templateFormBean") TemplateFormBean templateFormBean) {

        LOGGER.info("Appel de la page /gestion/template/update. Méthode traiterUpdate");
        ModelAndView mav = new ModelAndView("gestion/template/templateupdate");

        mav.addObject(TS_CODE_VAR, gouvPropertiesResolver.getDemarcheId());

        gestionTemplateService.saveTemplateForm(templateFormBean);
        mav.addObject("frOnly", isFrenchOnly());

        // Récupération à nouveau du template pour vérifier que tout est ok
        gestionTemplateService.retrieveTemplateForm(templateFormBean);

        mav.addObject(SharedMessages.SUCCESS_MESSAGES, MESSAGE_SUCCESS_MODIFICATION);

        LOGGER.info("======================= Fin /gestion/template/update. Méthode traiterUpdate");

        return mav;
    }

    private boolean isFrenchOnly() {
        // S'il n'y a qu'une langue on ne récupère que les templates FR
        return demarchesDataProvider.getLanguesDisponibles().size() == 1;
    }
}
