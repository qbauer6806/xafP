package mc.gouv.xaf.backweb.controller;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/gestion/properties")
@Secured("ROLE_CONFIGURATION")
@RequiredArgsConstructor
public class GestionPropertiesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionPropertiesController.class);
    private static final String REDIRECT = "redirect:/gestion/properties";
    private static final String MODIFIER_SUCCES = "La propriété a été modifiée.";

    private final PropertiesService propertiesService;
    private final AfBackUtils afBackUtils;

    @GetMapping
    public ModelAndView form(final RedirectAttributes redirectAttributes) {
        LOGGER.info("Appel de la page /gestion/properties. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/properties/properties");
        List<PropertiesDTO> properties = new ArrayList<>();
        if (afBackUtils.getDemarcheCanHandleProperties()) {
            properties = propertiesService.getAdminsFonctionnelsProperties();
        }
        mav.addObject("properties", properties);
        LOGGER.info("======================= Fin /gestion/properties. Méthode form");
        return mav;
    }

    @PostMapping(value = "/edit")
    @Transactional
    public ModelAndView modifier(@RequestParam Integer pkProperties, @RequestParam String value,
            final RedirectAttributes redirectAttributes) {
        String safeValue = AfBackUtils.logSafe(value);
        LOGGER.info("======================= Appel de la page /gestion/properties/modifier ({}, {})", pkProperties,
                safeValue);
        propertiesService.updatePropertyValue(pkProperties, value);
        List<String> messages = new ArrayList<>();
        messages.add(MODIFIER_SUCCES);
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
        ModelAndView mav = new ModelAndView(REDIRECT);

        LOGGER.info("======================= Fin /gestion/properties/modifier");
        return mav;
    }

}
