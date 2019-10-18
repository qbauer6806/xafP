package mc.gouv.xaf.backweb.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.DemandesCourriersService;
import mc.gouv.dem.shared.model.DemandeCourrierDTO;

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

    @Secured("ROLE_TRAITEMENT")
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView form() {

        LOGGER.info("======================= Appel de la page /gestion/courriers");

        List<DemandeCourrierDTO> courriers = demandesCourrierService.getCourriersPourDemarche(gouvPropertiesResolver.getDemarcheId());

        List<DemandeCourrierDTO> courriersEnAttente = new ArrayList<DemandeCourrierDTO>();
        List<DemandeCourrierDTO> courriersImprimes = new ArrayList<DemandeCourrierDTO>();

        for (DemandeCourrierDTO courrier : courriers) {
            if (courrier.getDatePrinted() != null) {
                courriersImprimes.add(courrier);
            } else {
                courriersEnAttente.add(courrier);
            }
        }

        LOGGER.info("======================= Fin /gestion/courriers");

        ModelAndView mav = new ModelAndView("gestion/courriers/gestioncourrier");
        mav.addObject("courriersEnAttente", courriersEnAttente);
        mav.addObject("courriersImprimes", courriersImprimes);
        return mav;
    }

    @Secured("ROLE_TRAITEMENT")
    @RequestMapping(value = "/print", method = RequestMethod.POST)
    public ModelAndView print(@RequestParam(required = true) Integer demandeId,
            @RequestParam(required = true) Integer courrierId, @RequestParam(required = false) String refCourrier)
            throws Exception {

        LOGGER.info("======================= Appel de la page /gestion/courriers/print (" + demandeId + "," + courrierId
                + ")");

        if (!StringUtils.isBlank(refCourrier)) {
            LOGGER.info("Appels à DEM pour mettre à jour la référence courrier...");
            DemandeCourrierDTO courrier = demandesCourrierService.getCourrier(gouvPropertiesResolver.getDemarcheId(), demandeId, courrierId);
            
            courrier.setIdentifiant(refCourrier);
            
            demandesCourrierService.updateCourrier(gouvPropertiesResolver.getDemarcheId(), demandeId, courrier);
        }

        LOGGER.info("Appel à DEM pour marquer le courrier comme imprimé...");
        demandesCourrierService.printCourrier(gouvPropertiesResolver.getDemarcheId(), demandeId, courrierId);

        ModelAndView mav = new ModelAndView("redirect:");

        LOGGER.info("======================= Fin /gestion/courriers/print");

        return mav;
    }

}
