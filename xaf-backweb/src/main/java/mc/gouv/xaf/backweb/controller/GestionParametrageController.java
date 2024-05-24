package mc.gouv.xaf.backweb.controller;

import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.formbean.ParametrageFormBean;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
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

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/gestion/parametrage")
@Secured("ROLE_CONFIGURATION")
public class GestionParametrageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionParametrageController.class);

    @Autowired
    private DemarchesService demarchesService;

    @Autowired
    private AfBackUtils afBackUtils;

    @GetMapping
    public ModelAndView form(@ModelAttribute("parametrageFormBean") ParametrageFormBean parametrageFormBean, final RedirectAttributes redirectAttributes) {
        LOGGER.info("Appel de la page /gestion/parametrage. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/parametrage/parametrage");
        if (afBackUtils.getDemarcheCanHandleProperties()) {
            DemarcheDTO demarche = afBackUtils.getDemarcheInfos();
            parametrageFormBean.setNomDemarche(demarche.getNom());
            parametrageFormBean.setEmailFrom(demarche.getEmailFrom());
            parametrageFormBean.setEmailFromNom(demarche.getEmailFromNom());
            parametrageFormBean.setEmailReplyto(demarche.getEmailReplyto());
            parametrageFormBean.setEmailReplytoNom(demarche.getEmailReplytoNom());
            parametrageFormBean.setEmailService(demarche.getEmailService());
            parametrageFormBean.setIdentifiantPrefixe(demarche.getIdentifiantPrefixe());

            parametrageFormBean.setNomDirection(demarche.getNomDirection());
            parametrageFormBean.setNomSousDirection(demarche.getNomSousDirection());
            parametrageFormBean.setNomFooter(demarche.getNomFooter());
            parametrageFormBean.setAdresseService(demarche.getAdresseService());
            parametrageFormBean.setNomSousDirectionComplement(demarche.getNomSousDirectionComplement());
            parametrageFormBean.setTelephoneService(demarche.getTelephoneService());

            if (demarche.getLangues().contains("fr")) {
                parametrageFormBean.setLangueFr(true);
            }
            if (demarche.getLangues().contains("en")) {
                parametrageFormBean.setLangueEn(true);
            }
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
        
        mav = new ModelAndView("redirect:/gestion/parametrage");

        DemarcheDTO demarche = afBackUtils.getDemarcheInfos();
        demarche.setNom(parametrageFormBean.getNomDemarche());
        demarche.setEmailFrom(parametrageFormBean.getEmailFrom());
        demarche.setEmailFromNom(parametrageFormBean.getEmailFromNom());
        demarche.setEmailReplyto(parametrageFormBean.getEmailReplyto());
        demarche.setEmailReplytoNom(parametrageFormBean.getEmailReplytoNom());
        demarche.setEmailService(parametrageFormBean.getEmailService());
        // remove space from identifiantPrefixe
        demarche.setIdentifiantPrefixe(StringUtils.deleteWhitespace(parametrageFormBean.getIdentifiantPrefixe()));
        demarche.setNomDirection(parametrageFormBean.getNomDirection());
        demarche.setNomSousDirection(parametrageFormBean.getNomSousDirection());
        demarche.setNomFooter(parametrageFormBean.getNomFooter());
        demarche.setAdresseService(parametrageFormBean.getAdresseService());
        demarche.setNomSousDirectionComplement(parametrageFormBean.getNomSousDirectionComplement());
        demarche.setTelephoneService(parametrageFormBean.getTelephoneService());

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
        demarche.setLangues(newLanguesList);
        demarchesService.updateDemarche(demarche);
        List<String> messages = new ArrayList<>();
        messages.add("Vous venez de modifier la démarche avec succès.");
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);

        LOGGER.info("======================= Fin /gestion/parametrage/save (POST)");
        
        return mav;
    }
    
}
