package mc.gouv.xaf.backweb.controller;

import static mc.gouv.xaf.back.service.data.impl.TraitementService.I18N_ENVOI_SUCCESS_CODE_MESSAGE;
import static mc.gouv.xaf.back.service.data.impl.TraitementService.I18N_TRAITEMENT_CONCURRENT_DEPOTIC_ERROR_CODE_MESSAGE;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.service.data.DemandesCommentaireService;
import mc.gouv.xaf.back.service.data.RectificationService;
import mc.gouv.xaf.back.service.data.impl.TraitementService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rectification")
@RequiredArgsConstructor
public class RectificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RectificationController.class);

    private final TraitementService traitementService;
    private final GouvBPM gouvBPM;
    private final DemandesCommentaireService demandesCommentaireService;
    private final RectificationService rectificationService;
    private final MessageSource messageSource;

    @Secured("ROLE_TRAITEMENT")
    @PostMapping("/repondre")
    @Transactional
    public ModelAndView repondre(@RequestParam String commentaireReponse, @RequestParam Integer pkDemande,
            @RequestParam String activeTaskDefinitionKey, final RedirectAttributes redirectAttributes) {
        String safeComm = commentaireReponse.replaceAll(SharedMessages.UNSAFE_CHARS, "_");
        LOGGER.info("Appel de la page /rectification/repondre commentaireReponse = {}", safeComm);

        GouvBPMTask activeTask = gouvBPM.getActiveTasksForDemande(pkDemande).getFirst();

        ModelAndView mav = traitementService.checkActiveTask(pkDemande, activeTask, activeTaskDefinitionKey,
                I18N_TRAITEMENT_CONCURRENT_DEPOTIC_ERROR_CODE_MESSAGE, redirectAttributes);
        if (mav != null) {
            return mav;
        }
        String agentId = AfBackUtils.getAuthenticatedAgentId();
        DemandeCommentaireDTO commInterne = new DemandeCommentaireDTO();
        commInterne.setAgentId(agentId);
        commInterne.setDate(new Date());
        commInterne.setFkDemandes(pkDemande);
        commInterne.setCommentaire("<b>Réponse à la demande de rectification : </b>" + commentaireReponse);
        demandesCommentaireService.putCommentaireInterne(commInterne);
        rectificationService.updateDemande(pkDemande, null, null, agentId);

        LOGGER.info("======================= Fin /rectification/repondre");

        List<String> messages = new ArrayList<>();
        messages.add(messageSource.getMessage(I18N_ENVOI_SUCCESS_CODE_MESSAGE, null, Locale.FRENCH));
        redirectAttributes.addFlashAttribute("successMessages", messages);
        return new ModelAndView("redirect:/demandes/" + pkDemande);
    }

}
