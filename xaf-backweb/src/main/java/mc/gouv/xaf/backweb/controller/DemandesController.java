package mc.gouv.xaf.backweb.controller;

import mc.gouv.logon.shared.User;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.backweb.dto.AgentAffichageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Controller pour la page /demandes
 * 
 * @author qdeme
 *
 */
@Controller
@RequestMapping("/demandes")
@Secured("ROLE_LECTURE")
public class DemandesController extends AbstractController {

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
            Collections.sort(agents, new AgentComparator());
        }
        List<AgentAffichageDTO> agentsAffichage = new ArrayList<AgentAffichageDTO>();
        for (User u : agents) {
            agentsAffichage.add(getAgentAffichageFromUser(u));
        }

        LOGGER.info("======================= Fin /demandes");

        ModelAndView mav = new ModelAndView("demandes/demandes");
        mav.addObject("agentsInit", agentsAffichage);
        mav.addObject("statuts", demarchesDataProvider.getStatusMap());
        mav.addObject("texte", texte);
        return mav;
    }

    public class AgentComparator implements Comparator<User> {

        @Override
        public int compare(User u1, User u2) {
            String u1Nom = "";
            if (u1.getNomAffichage() != null) {
                u1Nom = u1.getNomAffichage();
            } else {
                u1Nom = u1.getNom();
            }
            String u2Nom = "";
            if (u2.getNomAffichage() != null) {
                u2Nom = u2.getNomAffichage();
            } else {
                u2Nom = u2.getNom();
            }
            return u1Nom.compareTo(u2Nom);
        }

    }

    public String getDisplayNameFromUser(User u) {
        String displayName = "";
        if (u.getCivilite() != null) {
            displayName = u.getCivilite().getLibelle() + " ";
        }
        if (u.getPrenom() != null) {
            displayName += u.getPrenom() + " ";
        }

        if (u.getNomAffichage() != null) {
            displayName += u.getNomAffichage();
        } else {
            displayName += u.getNom();
        }
        return displayName;
    }

    private AgentAffichageDTO getAgentAffichageFromUser(User u) {
        AgentAffichageDTO a = new AgentAffichageDTO();
        a.setDisplayName(getDisplayNameFromUser(u));
        a.setMatricule(u.getMatricule());
        return a;
    }

}
