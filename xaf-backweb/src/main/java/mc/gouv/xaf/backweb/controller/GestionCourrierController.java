package mc.gouv.xaf.backweb.controller;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesCourriersService;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

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
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private DemandesCourriersService demandesCourrierService;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Secured({"ROLE_TRAITEMENT","ROLE_SAISIE"})
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView form() {

        LOGGER.info("======================= Appel de la page /gestion/courriers");

        ModelAndView mav = new ModelAndView("gestion/courriers/gestioncourrier");
        mav.addObject("statuts", demarchesDataProvider.getStatusMap());

        LOGGER.info("======================= Fin /gestion/courriers");
        return mav;
    }

    @Secured({"ROLE_TRAITEMENT","ROLE_SAISIE"})
    @RequestMapping(value = "/print", method = RequestMethod.POST)
    public ModelAndView print(@RequestParam(required = true) Integer demandeId,
            @RequestParam(required = true) Integer courrierId, @RequestParam(required = false) String refCourrier)
            throws Exception {

        LOGGER.info("======================= Appel de la page /gestion/courriers/print (" + demandeId + "," + courrierId
                + ")");

        LOGGER.info("Appels à DEM pour mettre à jour la référence courrier...");
        DemandeCourrierDTO courrier = demandesCourrierService.getCourrier(gouvPropertiesResolver.getDemarcheId(), demandeId, courrierId);

        courrier.setIdentifiant(refCourrier);
        courrier.setDatePrinted(new Date());

        demandesCourrierService.updateCourrier(gouvPropertiesResolver.getDemarcheId(), demandeId, courrier);

        ModelAndView mav = new ModelAndView("redirect:");

        LOGGER.info("======================= Fin /gestion/courriers/print");

        return mav;
    }

}
