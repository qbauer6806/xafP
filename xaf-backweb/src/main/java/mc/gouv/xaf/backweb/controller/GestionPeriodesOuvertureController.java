package mc.gouv.xaf.backweb.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import mc.gouv.xaf.shared.RequestConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PeriodesOuvertureService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;

@Controller
@RequestMapping("/gestion/periodesouverture")
@Secured({"ROLE_PARAMETRAGE", "ROLE_CONFIGURATION"})
public class GestionPeriodesOuvertureController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionPeriodesOuvertureController.class);
    private static final String AJOUTER_SUCCES = "La période d'ouverture a été ajoutée.";
    private static final String MODIFIER_SUCCES = "La période d'ouverture a été modifiée.";
    private static final String SUPPRIMER_SUCCES = "La période d'ouverture a été supprimée.";
    private static final String SUPPRIMER_TOUS_SUCCES = "Toutes les périodes d'ouverture ont été supprimées.";
    private static final String REDIRECT_PERIODES = "redirect:/gestion/periodesouverture?pageLength=";

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private PeriodesOuvertureService periodesOuvertureService;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat(AfBackUtils.DEFAULT_FRENCH_DATE_HOURS_FORMAT), false));
    }

    @GetMapping
    public ModelAndView form(@RequestParam(name = "pageLength", required = false) Integer pageLength, final RedirectAttributes redirectAttributes) {
        LOGGER.info("Appel de la page gestion/periodesouverture. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/periodesouverture/periodesouverture");
        if (null != pageLength) {
            mav.addObject("pageLength", pageLength);
        }
        LOGGER.info("======================= Fin /gestion/periodesouverture. Méthode form");
        return mav;
    }

    private ModelAndView redirectSuccess(Integer pageLengthNumber, RedirectAttributes redirectAttributes, String message) {
        ModelAndView mav = new ModelAndView(RequestConstant.REDIRECT);
        if (null != pageLengthNumber) {
            mav = new ModelAndView(REDIRECT_PERIODES + pageLengthNumber);
        }
        List<String> messages = new ArrayList<>();
        messages.add(message);
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
        return mav;
    }

    @PostMapping(value = "/edit", params = "action=ajouter")
    @Transactional
    public ModelAndView ajouter(@RequestParam Date periodeStartDate, @RequestParam Date periodeEndDate, @RequestParam Integer pageLengthNumber,
                                final RedirectAttributes redirectAttributes) {

        LOGGER.info("======================= Appel de la page /gestion/periodesouverture/ajouter ({}, {})", periodeStartDate, periodeEndDate);
        PeriodeOuvertureDTO periode = new PeriodeOuvertureDTO();
        periode.setDateDebut(periodeStartDate);
        periode.setDateFin(periodeEndDate);
        periode.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        periodesOuvertureService.saveOrUpdatePeriodeOuverture(gouvPropertiesResolver.getDemarcheId(), periode);
        ModelAndView mav = redirectSuccess(pageLengthNumber, redirectAttributes, AJOUTER_SUCCES);
        LOGGER.info("======================= Fin /gestion/periodesouverture/ajouter");
        return mav;
    }

    @PostMapping(value = "/edit", params = "action=modifier")
    @Transactional
    public ModelAndView modifier(@RequestParam Date periodeStartDate, @RequestParam Date periodeEndDate, @RequestParam Integer pkPeriodesOuverture,
                                 @RequestParam Integer pageLengthNumber, final RedirectAttributes redirectAttributes) {

        LOGGER.info("======================= Appel de la page /gestion/periodesouverture/modifier ({}, {}, {})", periodeStartDate, periodeEndDate, pkPeriodesOuverture);
        PeriodeOuvertureDTO periode = new PeriodeOuvertureDTO();
        periode.setDateDebut(periodeStartDate);
        periode.setDateFin(periodeEndDate);
        periode.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        periode.setPkPeriodesOuverture(pkPeriodesOuverture);
        periodesOuvertureService.saveOrUpdatePeriodeOuverture(gouvPropertiesResolver.getDemarcheId(), periode);
        ModelAndView mav = redirectSuccess(pageLengthNumber, redirectAttributes, MODIFIER_SUCCES);
        LOGGER.info("======================= Fin /gestion/periodesouverture/modifier");
        return mav;
    }

    @PostMapping(value = "/supprimer")
    @Transactional
    public ModelAndView supprimer(@RequestParam Integer pkPeriodesOuverture, @RequestParam Integer pageLengthNumber, final RedirectAttributes redirectAttributes) {
        LOGGER.info("======================= Appel de la page /gestion/periodesouverture/supprimer ({})", pkPeriodesOuverture);
        periodesOuvertureService.deletePeriodeOuverture(gouvPropertiesResolver.getDemarcheId(), pkPeriodesOuverture);
        ModelAndView mav = redirectSuccess(pageLengthNumber, redirectAttributes, SUPPRIMER_SUCCES);
        LOGGER.info("======================= Fin /gestion/periodesouverture/supprimer");
        return mav;
    }

    @PostMapping(value = "/supprimertous")
    @Transactional
    public ModelAndView supprimerTous(final RedirectAttributes redirectAttributes) {
        LOGGER.info("======================= Appel de la page /gestion/periodesouverture/supprimertous");
        periodesOuvertureService.deleteAllPeriodeOuverture(gouvPropertiesResolver.getDemarcheId());
        ModelAndView mav = new ModelAndView(RequestConstant.REDIRECT);
        List<String> messages = new ArrayList<>();
        messages.add(SUPPRIMER_TOUS_SUCCES);
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
        LOGGER.info("======================= Fin /gestion/periodesouverture/supprimertous");
        return mav;
    }
}
