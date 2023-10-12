package mc.gouv.xaf.backweb.controller;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.formbean.ParametrageFormBean;
import mc.gouv.xaf.shared.dto.DemarcheDTO;

@Controller
@RequestMapping("/gestion/parametrage")
@Secured("ROLE_CONFIGURATION")
public class GestionParametrageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionParametrageController.class);
    private static final String REDIRECT = "redirect:/gestion/parametrage";

    @Autowired
    private PropertiesService propertiesService;
    
    @Autowired
    private DemarchesService demarchesService;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private AfBackUtils afBackUtils;

    @GetMapping
    public ModelAndView form(@ModelAttribute("parametrageFormBean") ParametrageFormBean parametrageFormBean, final RedirectAttributes redirectAttributes) {
        LOGGER.info("Appel de la page /gestion/parametrage. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/parametrage/parametrage");
        
        DemarcheDTO demarche = demarchesService.getDemarche(gouvPropertiesResolver.getDemarcheId());
        parametrageFormBean.setNomDemarche(demarche.getNom());
        parametrageFormBean.setEmailFrom(demarche.getEmailFrom());
        parametrageFormBean.setEmailFromNom(demarche.getEmailFromNom());
        parametrageFormBean.setEmailReplyto(demarche.getEmailReplyto());
        parametrageFormBean.setEmailReplytoNom(demarche.getEmailReplytoNom());
        parametrageFormBean.setEmailService(demarche.getEmailService());
        parametrageFormBean.setEmailServiceNom(demarche.getEmailServiceNom());
        parametrageFormBean.setIdentifiantPrefixe(demarche.getIdentifiantPrefixe());
        if (demarche.getLangues().contains("fr")) {
        	parametrageFormBean.setLangueFr(true);
        }
        if (demarche.getLangues().contains("en")) {
        	parametrageFormBean.setLangueEn(true);
        }
        if (demarche.getLangues().contains("it")) {
        	parametrageFormBean.setLangueIt(true);
        }

        LOGGER.info("======================= Fin /gestion/parametrage. Méthode form");
        return mav;
    }

    /**
     * Création de l'usager courrier depuis le formulaire de création (POST)
     */
    @Secured({"ROLE_CONFIGURATION"})
    @PostMapping(value = "/sauvegarder", params = "action=Sauvegarder")
    public ModelAndView sauvegarderParametrage(
            @Valid @ModelAttribute("parametrageFormBean") ParametrageFormBean parametrageFormBean,
            BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {

        ModelAndView mav;
        LOGGER.info("======================= Appel de la page /gestion/parametrage/save (POST)");
        
        mav = new ModelAndView("redirect:");
        
        DemarcheDTO demarche = demarchesService.getDemarche(gouvPropertiesResolver.getDemarcheId());
        demarche.setNom(parametrageFormBean.getNomDemarche());
        demarche.setEmailFrom(parametrageFormBean.getEmailFrom());
        demarche.setEmailFromNom(parametrageFormBean.getEmailFromNom());
        demarche.setEmailReplyto(parametrageFormBean.getEmailReplyto());
        demarche.setEmailReplytoNom(parametrageFormBean.getEmailReplytoNom());
        demarche.setEmailService(parametrageFormBean.getEmailService());
        demarche.setEmailServiceNom(parametrageFormBean.getEmailServiceNom());
        demarche.setIdentifiantPrefixe(parametrageFormBean.getIdentifiantPrefixe());
        String newLanguesList = "";
        if (parametrageFormBean.getLangueFr()) {
        	newLanguesList = "fr";
        }
        if (parametrageFormBean.getLangueEn()) {
        	if (StringUtils.isNotBlank(newLanguesList)) {
        		newLanguesList += ",";
        	}
        	newLanguesList += "en";
        }
        if (parametrageFormBean.getLangueIt()) {
        	if (StringUtils.isNotBlank(newLanguesList)) {
        		newLanguesList += ",";
        	}
        	newLanguesList += "it";
        }
        demarche.setLangues(newLanguesList);
        demarchesService.updateDemarche(demarche);

        LOGGER.info("======================= Fin /gestion/parametrage/save (POST)");
        
        return mav;
    }
    
}
