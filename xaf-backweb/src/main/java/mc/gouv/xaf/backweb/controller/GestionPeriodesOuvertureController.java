package mc.gouv.xaf.backweb.controller;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

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
import mc.gouv.xaf.back.service.data.PeriodesOuvertureService;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;

@Controller
@RequestMapping("/gestion/periodesouverture")
@Secured("ROLE_PARAMETRAGE")
public class GestionPeriodesOuvertureController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionModelesController.class);
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private PeriodesOuvertureService periodesOuvertureService;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView form() throws Exception {

        LOGGER.info("Appel de la page gestion/modeles. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/periodesouverture/periodesouverture");
        
        List<PeriodeOuvertureDTO> periodes = periodesOuvertureService.getPeriodesOuverture(gouvPropertiesResolver.getDemarcheId());
        
        mav.addObject("periodes", periodes);

        LOGGER.info("======================= Fin /gestion/modeles. Méthode form");

        return mav;
    }
    
    @RequestMapping(value = "/ajouter", method = RequestMethod.POST)
    @Transactional
    public ModelAndView ajouter(@RequestParam(required = true) Date periodeStartDate, @RequestParam(required = true) Date periodeEndDate)
            throws Exception {

        LOGGER.info("======================= Appel de la page /gestion/periodesouverture/ajouter (" + periodeStartDate + "," + periodeEndDate + ")");
        PeriodeOuvertureDTO periode = new PeriodeOuvertureDTO();
        periode.setDateDebut(periodeStartDate);
        periode.setDateFin(periodeEndDate);
        periode.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        periodesOuvertureService.saveOrUpdatePeriodeOuverture(gouvPropertiesResolver.getDemarcheId(), periode);

        ModelAndView mav = new ModelAndView("redirect:");

        LOGGER.info("======================= Fin /gestion/periodesouverture/ajouter");

        return mav;
        
    }
    
    @RequestMapping(value = "/modifier", method = RequestMethod.POST)
    @Transactional
    public ModelAndView modifier(@RequestParam(required = true) Date periodeStartDate0, @RequestParam(required = true) Date periodeEndDate0, @RequestParam(required = true) Integer pkPeriodesOuverture)
            throws Exception {

        LOGGER.info("======================= Appel de la page /gestion/periodesouverture/modifier (" + periodeStartDate0 + "," + periodeEndDate0 + "," + pkPeriodesOuverture + ")");
        PeriodeOuvertureDTO periode = new PeriodeOuvertureDTO();
        periode.setDateDebut(periodeStartDate0);
        periode.setDateFin(periodeEndDate0);
        periode.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        periode.setPkPeriodesOuverture(pkPeriodesOuverture);
        periodesOuvertureService.saveOrUpdatePeriodeOuverture(gouvPropertiesResolver.getDemarcheId(), periode);

        ModelAndView mav = new ModelAndView("redirect:");

        LOGGER.info("======================= Fin /gestion/periodesouverture/modifier");

        return mav;
        
    }
    
    @RequestMapping(value = "/supprimer", method = RequestMethod.POST)
    @Transactional
    public ModelAndView supprimer(@RequestParam(required = true) Integer pkPeriodesOuverture0)
            throws Exception {

        LOGGER.info("======================= Appel de la page /gestion/periodesouverture/supprimer (" + pkPeriodesOuverture0 + ")");
        PeriodeOuvertureDTO periode = new PeriodeOuvertureDTO();
        periode.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        periode.setPkPeriodesOuverture(pkPeriodesOuverture0);
        periodesOuvertureService.deletePeriodeOuverture(gouvPropertiesResolver.getDemarcheId(), pkPeriodesOuverture0);

        ModelAndView mav = new ModelAndView("redirect:");

        LOGGER.info("======================= Fin /gestion/periodesouverture/supprimer");

        return mav;
        
    }

}
