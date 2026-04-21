package mc.gouv.xaf.backweb.controller;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.purge.PurgeDemandesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
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
@RequestMapping("/gestion/purgeselective")
@Secured("ROLE_CONFIGURATION")
@RequiredArgsConstructor
public class GestionPurgeSelectiveController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionPurgeSelectiveController.class);

    private final DemandesService demandesService;
    private final PurgeDemandesService purgeDemandesService;
    private final DemarchesDataProvider demarchesDataProvider;
    private final UtilisateursUtils utilisateursUtils;

    @GetMapping
    public ModelAndView form() {
        LOGGER.info("======================= Appel de la page /gestion/purgeselective");
        return new ModelAndView("gestion/purgeselective/purgeselective");
    }

    @PostMapping
    public ModelAndView traiterDemande(@RequestParam("identifiants") List<String> identifiants,
            final RedirectAttributes redirectAttributes) {
        List<String> successMessages = new ArrayList<>();
        List<String> warningMessages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (String identifiant : identifiants) {
            LOGGER.info("Purge de l'identifiant {}", AfBackUtils.logSafe(identifiant));
            if (identifiant == null || identifiant.isBlank()) {
                warningMessages.add("Identifiant vide ignoré");
                continue;
            }

            try {
                DemandeDTO demandeDTO = demandesService.getDemande(identifiant);

                if (demandeDTO == null) {
                    warningMessages.add("La demande " + identifiant + " n'existe pas.");
                    continue;
                }

                if (!demarchesDataProvider.getStatutsAPurger()
                        .contains(demandeDTO.getDernierStatut().getName())) {
                    warningMessages.add("La demande " + identifiant + " n'est pas dans un statut final et n'a pas été purgée.");
                    continue;
                }
                String origineSuppression = utilisateursUtils.getUserNameFromID(AfBackUtils.getAuthenticatedAgentId());
                purgeDemandesService.deleteDemandePurgeSelective(demandeDTO.getPkDemandes(), origineSuppression);
                successMessages.add("La demande " + identifiant + " a été purgée avec succès.");

            } catch (Exception e) {
                LOGGER.error("Erreur lors de la purge de {}", identifiant, e);
                errorMessages.add("Erreur lors de la purge de " + identifiant + " : " + e.getMessage());
            }
        }

        // purge des fichiers seulement si au moins une demande a été supprimée
        if (!successMessages.isEmpty()) {
            purgeDemandesService.executerPurgeFichiers();
        }

        if (!successMessages.isEmpty()) {
            redirectAttributes.addFlashAttribute("successMessages", successMessages);
        }
        if (!warningMessages.isEmpty()) {
            redirectAttributes.addFlashAttribute("warningMessages", warningMessages);
        }
        if (!errorMessages.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
        }

        return new ModelAndView("redirect:purgeselective");
    }
}
