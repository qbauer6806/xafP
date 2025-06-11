package mc.gouv.xaf.backweb.ws;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.impl.AfMailTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.formbean.PreviewFormBean;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/ws/mailpreview")
public class MailPreviewController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailPreviewController.class);

    @Autowired
    private MailService mailService;

    @Autowired
    private AfMailTemplateModelProvider afMailTemplateModelProvider;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private GouvBPM gouvBPM;

    private ModelAndView buildMailPreview(String action, String codeMotifChoisi, Integer pkDemande, String commentaire)
            throws IOException {
        String templateCode = afMailTemplateModelProvider.getMailTemplateCodeForAction(action);
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

    @PostMapping(consumes = "application/json")
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

}
