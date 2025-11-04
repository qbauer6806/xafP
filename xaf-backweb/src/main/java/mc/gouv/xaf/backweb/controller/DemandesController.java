package mc.gouv.xaf.backweb.controller;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.back.service.utils.AgentComparator;
import mc.gouv.xaf.backweb.dto.AgentAffichageDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller pour la page /demandes
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/demandes")
@Secured("ROLE_LECTURE")
@RequiredArgsConstructor
public class DemandesController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesController.class);

    private final UtilisateursCache utilisateursCache;
    private final DemarchesDataProvider demarchesDataProvider;

    @GetMapping
    public ModelAndView form(@RequestParam(value = "statut", required = false) String statut,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam(required = false) String texte) {

        LOGGER.info("======================= Appel de la page /demandes");

        List<User> agents = new ArrayList<>(utilisateursCache.getAll().values());

        // Tri des agents par nom
        if (!agents.isEmpty()) {
            agents.sort(new AgentComparator());
        }
        List<AgentAffichageDTO> agentsAffichage = new ArrayList<>();
        for (User u : agents) {
            agentsAffichage.add(getAgentAffichageFromUser(u));
        }

        LOGGER.info("======================= Fin /demandes");

        ModelAndView mav = new ModelAndView("demandes/demandes");
        mav.addObject("agentsInit", agentsAffichage);
        mav.addObject("statuts", demarchesDataProvider.getStatusMap());
        mav.addObject("texte", texte);
        DemandeRechercheDTO demandeRechercheDTO = demarchesDataProvider.getDemandeRecherche();
        if (demandeRechercheDTO != null) {
            mav.addObject("demandeRechercheData",
                    demandeRechercheDTO.getData() != null ? demandeRechercheDTO.getData().generateParamUrl() : null);
            mav.addObject("demandeRechercheCreationStartDate", demandeRechercheDTO.getCreationStartDate() != null
                    ? demandeRechercheDTO.getCreationStartDate()
                    : null);
            mav.addObject("demandeRechercheCreationEndDate",
                    demandeRechercheDTO.getCreationEndDate() != null ? demandeRechercheDTO.getCreationEndDate() : null);
        }

        return mav;
    }

    public String getDisplayNameFromUser(User u) {
        String displayName = "";
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
