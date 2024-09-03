package mc.gouv.xaf.backweb.controller;

import java.util.Date;
import mc.gouv.xaf.back.service.data.DemandesCourriersService;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller pour la page /gestioncourrier
 * 
 * @author qdeme
 *
 */
@Controller
@RequestMapping("/gestion/courriers")
public class GestionCourrierController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionCourrierController.class);
    
    @Autowired
    private DemandesCourriersService demandesCourrierService;

    @Secured({"ROLE_TRAITEMENT","ROLE_SAISIE"})
    @GetMapping
    public ModelAndView form() {
        LOGGER.info("======================= Appel de la page /gestion/courriers");
        ModelAndView mav = new ModelAndView("gestion/courriers/gestioncourrier");
        LOGGER.info("======================= Fin /gestion/courriers");
        return mav;
    }

    @Secured({"ROLE_TRAITEMENT","ROLE_SAISIE"})
    @PostMapping(value = "/print")
    public ModelAndView print(@RequestParam Integer demandeId, @RequestParam Integer courrierId,
                              @RequestParam(required = false) String refCourrier) {

        LOGGER.info("======================= Appel de la page /gestion/courriers/print ({}, {})", demandeId, courrierId);

        LOGGER.info("Appels à DEM pour mettre à jour la référence courrier...");
        DemandeCourrierDTO courrier = demandesCourrierService.getCourrier(demandeId, courrierId);

        courrier.setIdentifiant(StringEscapeUtils.escapeHtml4(refCourrier));
        courrier.setDatePrinted(new Date());

        demandesCourrierService.updateCourrier(demandeId, courrier);

        ModelAndView mav = new ModelAndView("redirect:");

        LOGGER.info("======================= Fin /gestion/courriers/print");

        return mav;
    }

}
