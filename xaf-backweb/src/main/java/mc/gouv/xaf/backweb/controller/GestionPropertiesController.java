package mc.gouv.xaf.backweb.controller;

import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.formbean.PropertiesFormBean;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.PropertiesTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/gestion/properties")
@Secured("ROLE_CONFIGURATION")
public class GestionPropertiesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionPropertiesController.class);
    private static final String REDIRECT = "redirect:/gestion/properties";
    private static final String SUCCESS_MESSAGES = "successMessages";
    private static final String AJOUTER_SUCCES = "La propriété a été ajoutée.";
    private static final String MODIFIER_SUCCES = "La propriété a été modifiée.";
    private static final String SUPPRIMER_SUCCES = "La propriété a été supprimée.";

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private AfBackUtils afBackUtils;

    @GetMapping
    public ModelAndView form(@ModelAttribute("propertiesFormBean") PropertiesFormBean propertiesFormBean,
                             final RedirectAttributes redirectAttributes) {
        LOGGER.info("Appel de la page /gestion/properties. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/properties/properties");
        List<PropertiesDTO> properties = new ArrayList<>();
        if (afBackUtils.getDemarcheCanHandleProperties()) {
            properties = propertiesService.getProperties();
        }
        mav.addObject("properties", properties);

//        if (propertiesFormBean != null) {
//            mav.addObject("propertiesFormBean", propertiesFormBean);
//        }
//
//        if (StringUtils.isNotBlank(action)) {
//            mav.addObject("action", action);
//        }

        LOGGER.info("======================= Fin /gestion/properties. Méthode form");
        return mav;
    }

    @PostMapping(value = "/edit", params = "action=ajouter")
    @Transactional
    public ModelAndView ajouter(@Valid @ModelAttribute("propertiesFormBean") PropertiesFormBean propertiesFormBean,
                                BindingResult result, final RedirectAttributes redirectAttributes) {
        String value = propertiesFormBean.getValue();
        String key = propertiesFormBean.getKey().toUpperCase();
        String type = propertiesFormBean.getType();

        LOGGER.info("======================= Appel de la page /gestion/properties/ajouter ({}, {}, {})", type, key, value);

        PropertiesDTO properties = new PropertiesDTO();
        properties.setValue(value);
        properties.setKey(key);
        properties.setType(PropertiesTypeEnum.valueOf(type));
        ModelAndView mav = saveOrUpdateProperties(properties, true, result, redirectAttributes);

        LOGGER.info("======================= Fin /gestion/properties/ajouter");
        return mav;
    }

    @PostMapping(value = "/edit", params = "action=modifier")
    @Transactional
    public ModelAndView modifier(@Valid @ModelAttribute("propertiesFormBean") PropertiesFormBean propertiesFormBean,
                                 BindingResult result, final RedirectAttributes redirectAttributes) {

        Integer pkProperties = propertiesFormBean.getPkProperties();
        String value = propertiesFormBean.getValue();
        String key = propertiesFormBean.getKey().toUpperCase();
        String type = propertiesFormBean.getType();

        LOGGER.info("======================= Appel de la page /gestion/properties/modifier ({}, {}, {}, {})", pkProperties, type, key, value);

        PropertiesDTO properties = new PropertiesDTO();
        properties.setValue(value);
        properties.setKey(key);
        properties.setType(PropertiesTypeEnum.valueOf(type));
        properties.setPkProperties(pkProperties);
        ModelAndView mav = saveOrUpdateProperties(properties, false, result, redirectAttributes);

        LOGGER.info("======================= Fin /gestion/properties/modifier");
        return mav;
    }

    private ModelAndView saveOrUpdateProperties(PropertiesDTO dto, boolean isCreate, BindingResult result, final RedirectAttributes redirectAttributes) {
        boolean isValid = propertiesService.checkProperty(dto, isCreate);
        if (!isValid) {
            FieldError fe = new FieldError("propertiesFormBean", "key",
                    "La clé est déjà existante dans la base de données");
            result.addError(fe);
        }

        ModelAndView mav = new ModelAndView(REDIRECT);
        if (result.hasErrors()) {
            List<String> errors = new ArrayList<>();
            errors.add(AfBackUtils.MESSAGE_ERREURS_FORMULAIRE);
            redirectAttributes.addFlashAttribute("errorMessages", errors);
//            mav.addObject("action", "modifier");
//            mav.addObject("propertiesFormBean", propertiesFormBean);
        } else {
            propertiesService.saveOrUpdateProperties(dto);
            List<String> messages = new ArrayList<>();
            String success = isCreate ? AJOUTER_SUCCES : MODIFIER_SUCCES;
            messages.add(success);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGES, messages);
        }

        return mav;
    }

    @PostMapping(value = "/supprimer")
    @Transactional
    public ModelAndView supprimer(@RequestParam Integer pkProperties,
                                  final RedirectAttributes redirectAttributes) {
        LOGGER.info("======================= Appel de la page /gestion/periodesouverture/supprimer ");
        propertiesService.deleteProperties(pkProperties);
        ModelAndView mav = new ModelAndView(REDIRECT);
        List<String> messages = new ArrayList<>();
        messages.add(SUPPRIMER_SUCCES);
        redirectAttributes.addFlashAttribute(SUCCESS_MESSAGES, messages);
        LOGGER.info("======================= Fin /gestion/periodesouverture/supprimer");
        return mav;
    }

}
