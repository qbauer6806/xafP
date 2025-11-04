package mc.gouv.xaf.backweb.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.itg.sms.SmsTemplatesService;
import mc.gouv.xaf.back.service.templates.GestionSmsTemplateService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.SmsTemplateDTO;
import mc.gouv.xaf.shared.formbean.SmsTemplateFormBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller pour la modification de templates de SMS
 *
 * @author qdeme
 */
@Controller
@Secured({ "ROLE_CONFIGURATION" })
@RequestMapping("/gestion/smstemplate")
@RequiredArgsConstructor
public class GestionSmsTemplateController extends AbstractController {

    private final GestionSmsTemplateService gestionSmsTemplateService;

    private final BackGouvPropertiesResolver gouvPropertiesResolver;

    private final SmsTemplatesService smsTemplatesService;

    private final AfBackUtils afBackUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionSmsTemplateController.class);
    private static final String TS_CODE_VAR = "tsCode";
    private static final String FR_ONLY_VAR = "frOnly";

    // Messages
    private static final String MESSAGE_SUCCESS_MODIFICATION = "Le template SMS a été modifié avec succès";

    @GetMapping
    public ModelAndView getTemplates() {
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info("Appel de la page /gestion/smstemplate. Méthode getTemplates");
        ModelAndView mav = new ModelAndView("gestion/smstemplate/smstemplate");
        boolean frOnly = isFrenchOnly();
        mav.addObject(TS_CODE_VAR, demarcheId);
        mav.addObject(FR_ONLY_VAR, frOnly);
        List<SmsTemplateDTO> templateList = frOnly ? smsTemplatesService.getTemplates("fr") : smsTemplatesService.getTemplates();
        mav.addObject("templateList", templateList);
        LOGGER.info("======================= Fin /gestion/smstemplate. Méthode getTemplates");
        return mav;
    }

    @GetMapping(path = "/update")
    public ModelAndView formUpdateInit(@ModelAttribute("smsTemplateFormBean") SmsTemplateFormBean smsTemplateFormBean) {
        LOGGER.info("Appel de la page /gestion/smstemplate/update. Méthode formUpdateInit");
        ModelAndView mav = new ModelAndView("gestion/smstemplate/smstemplateupdate");
        mav.addObject(TS_CODE_VAR, gouvPropertiesResolver.getDemarcheId());
        mav.addObject(FR_ONLY_VAR, isFrenchOnly());
        gestionSmsTemplateService.retrieveTemplateForm(smsTemplateFormBean);
        LOGGER.info("======================= Fin /gestion/smstemplate/update. Méthode formUpdateInit");
        return mav;
    }

    @PostMapping(path = "/update")
    public ModelAndView traiterUpdate(@ModelAttribute("smsTemplateFormBean") SmsTemplateFormBean smsTemplateFormBean) {
        LOGGER.info("Appel de la page /gestion/smstemplate/update. Méthode traiterUpdate");
        ModelAndView mav = new ModelAndView("gestion/smstemplate/smstemplateupdate");
        mav.addObject(TS_CODE_VAR, gouvPropertiesResolver.getDemarcheId());
        gestionSmsTemplateService.saveTemplateForm(smsTemplateFormBean);
        mav.addObject(FR_ONLY_VAR, isFrenchOnly());
        // Récupération à nouveau du template pour vérifier que tout est ok
        gestionSmsTemplateService.retrieveTemplateForm(smsTemplateFormBean);
        mav.addObject(SharedMessages.SUCCESS_MESSAGES, MESSAGE_SUCCESS_MODIFICATION);
        LOGGER.info("======================= Fin /gestion/smstemplate/update. Méthode traiterUpdate");
        return mav;
    }

    private boolean isFrenchOnly() {
        // S'il n'y a qu'une langue on ne récupère que les templates FR
        Map<String, String> langues = afBackUtils.getLanguesDisponibles();
        return langues.size() == 1 && langues.containsKey("fr");
    }
}
