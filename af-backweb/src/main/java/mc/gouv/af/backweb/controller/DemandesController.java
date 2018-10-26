package mc.gouv.af.backweb.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import mc.gouv.af.back.cache.UsagersCache;
import mc.gouv.af.back.cache.UtilisateursCache;
import mc.gouv.af.back.service.DemarchesDataProvider;
import mc.gouv.af.back.util.UserComparator;
import mc.gouv.logon.shared.User;
import mc.gouv.servicerest.usager.model.UsagerBean;

/**
 * Controller pour la page /demandes
 * 
 * @author qdeme
 *
 */
@Controller
@RequestMapping("/demandes")
public class DemandesController extends AbstractController {

    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private UtilisateursCache utilisateursCache;

    @Autowired
    DemarchesDataProvider demarchesDataProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesController.class);

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView form(@RequestParam(value = "statut", required = false) String statut,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam(required = false) String texte) {

        LOGGER.info("======================= Appel de la page /demandes");

        List<User> agents = new ArrayList<User>(utilisateursCache.getAll().values());

        // Tri des agents par nom
        if (agents != null) {
            Collections.sort(agents, new UserComparator());
        }

        Collection<UsagerBean> usagers = usagersCache.getAll().values();

        LOGGER.info("======================= Fin /demandes");

        ModelAndView mav = new ModelAndView("demandes/demandes");
        mav.addObject("agentsInit", agents);
        mav.addObject("statuts", demarchesDataProvider.getStatusMap());
        mav.addObject("usagersInit", usagers);
        mav.addObject("texte", texte);
        return mav;
    }

}
