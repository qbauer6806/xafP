package mc.gouv.xaf.backweb.ws;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.impl.AfMailTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.formbean.PreviewFormBean;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Sert à générer la preview des emails
 *
 * @author qdeme
 */
@GouvRestController
@Secured("ROLE_LECTURE")
@RequestMapping("/ws")
@RequiredArgsConstructor
public class MailPreviewController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailPreviewController.class);

    private final MailService mailService;

    private final AfMailTemplateModelProvider afMailTemplateModelProvider;

    private final DemandesService demandesService;

    private final GouvBPM gouvBPM;

    private ModelAndView buildMailPreview(String action, String codeMotifChoisi, Integer pkDemande, String commentaire)
            throws IOException {
        return buildMailPreviewByCode(
                afMailTemplateModelProvider.getMailTemplateCodeForAction(action, demandesService.getDemande(pkDemande)),
                codeMotifChoisi, pkDemande, commentaire);
    }

    private ModelAndView buildMailPreviewByCode(String templateCode, String codeMotifChoisi, Integer pkDemande, String commentaire)
            throws IOException {
        String bodyTemplateCode = templateCode + "_CORPS";
        String subjectTemplateCode = templateCode + "_OBJET";

        Map<String, Object> bpmVariables = gouvBPM.getProcessBusinessVariables(pkDemande);

        DemandeDTO demande = demandesService.getDemande(pkDemande);

        // Remplacement des sauts de ligne par des balises <br> pour un affichage HTML correct
        commentaire = AfBackUtils.formatCommentaire(commentaire);
        Map<String, Object> model = afMailTemplateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode, demande,
                bpmVariables, codeMotifChoisi, commentaire);

        LOGGER.info("Génération de l'aperçu de l'email...");
        String[] preview = mailService.getMailPreview(bodyTemplateCode, subjectTemplateCode, demande.getLangue(),
                model);

        ModelAndView mav = new ModelAndView("misc/mailpreview");
        mav.addObject("mailSubject", preview[0]);
        mav.addObject("mailBody", preview[1]);

        return mav;
    }

    private ModelAndView buildMailPreviewByText(String subjectTemplateText, String bodyTemplateText, String codeMotifChoisi, Integer pkDemande, String commentaire)
            throws IOException {

        Map<String, Object> bpmVariables = gouvBPM.getProcessBusinessVariables(pkDemande);

        DemandeDTO demande = demandesService.getDemande(pkDemande);

        // Remplacement des sauts de ligne par des balises <br> pour un affichage HTML correct
        commentaire = AfBackUtils.formatCommentaire(commentaire);
        // TODO Changer la méthode getModel pour ne pas tenir compte du template code car pas forcément utile. Le modèle ne doit pas être conditionné par le mail
        Map<String, Object> model = afMailTemplateModelProvider.getModel("", "", demande,
                bpmVariables, codeMotifChoisi, commentaire);

        LOGGER.info("Génération de l'aperçu de l'email...");
        String[] preview = mailService.getMailPreviewByText(bodyTemplateText, subjectTemplateText, demande.getLangue(),
                model);

        ModelAndView mav = new ModelAndView("misc/mailpreview");
        mav.addObject("mailSubject", preview[0]);
        mav.addObject("mailBody", preview[1]);

        return mav;
    }

    @PostMapping(value = "/mailpreview", consumes = "application/json")
    public ModelAndView mailpreview(@Valid @RequestBody PreviewFormBean mailPreviewFormBean) throws IOException {
        String action = mailPreviewFormBean.getAction();
        String codeMotifChoisi = mailPreviewFormBean.getCodeMotifChoisi();
        Integer pkDemande = mailPreviewFormBean.getPkDemande();
        String commentaire = mailPreviewFormBean.getCommentaire();
        String safeAction = AfBackUtils.logSafe(action);
        String safeCodeMotifChoisi = AfBackUtils.logSafe(codeMotifChoisi);
        String safeCommentaire = AfBackUtils.logSafe(commentaire);
        LOGGER.info("======================= Appel de /ws/mailpreview ({}, {}, {}, {})", safeAction,
                safeCodeMotifChoisi, pkDemande, safeCommentaire);
        ModelAndView mav = buildMailPreview(action, codeMotifChoisi, pkDemande, commentaire);
        LOGGER.info("======================= Fin /ws/mailpreview");
        return mav;

    }

    @PostMapping(value = "/mailpreview-by-text", consumes = "application/json")
    public ModelAndView mailPreviewByText(@Valid @RequestBody PreviewFormBean mailPreviewFormBean) throws IOException {

        if (StringUtils.isBlank(mailPreviewFormBean.getTemplateText()) || StringUtils.isBlank(mailPreviewFormBean.getSubjectText()) ) {
            throw new DemarcheException("Le sujet et le corps du mail sont obligatoires.");
        }

        String codeMotifChoisi = mailPreviewFormBean.getCodeMotifChoisi();
        Integer pkDemande = mailPreviewFormBean.getPkDemande();
        String commentaire = mailPreviewFormBean.getCommentaire();
        String safeSubjectText = AfBackUtils.logSafe(mailPreviewFormBean.getSubjectText());
        String safeTemplateText = AfBackUtils.logSafe(mailPreviewFormBean.getTemplateText());
        String safeCodeMotifChoisi = AfBackUtils.logSafe(codeMotifChoisi);
        String safeCommentaire = AfBackUtils.logSafe(commentaire);
        LOGGER.info("======================= Appel de /ws/mailpreview-by-text ({}, {}, {})",
                safeCodeMotifChoisi, pkDemande, safeCommentaire);
        ModelAndView mav = buildMailPreviewByText(safeSubjectText, safeTemplateText, codeMotifChoisi, pkDemande, commentaire);
        LOGGER.info("======================= Fin /ws/mailpreview-by-text");
        return mav;

    }
}
