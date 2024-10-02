package mc.gouv.xaf.backweb.ws;

import java.util.Map;
import java.util.Map.Entry;

import jakarta.validation.Valid;

import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.formbean.PreviewFormBean;

/**
 * 
 * Sert à générer la preview des emails
 * 
 * @author qdeme
 *
 */
@Controller
@RequestMapping("/ws/mailpreview")
public class MailPreviewController extends AbstractController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailPreviewController.class);

	@Autowired
	private MailService mailService;

	@Autowired
	private MailTemplateModelProvider mailTemplateModelProvider;

	@Autowired
	private DemandesService demandesService;

    @Autowired
	private GouvBPM gouvBPM;

	private ModelAndView buildMailPreview(String action, String codeMotifChoisi, Integer pkDemande, String commentaire)
			throws Exception {
		Entry<String, String> templateCodes = mailTemplateModelProvider.getMailTemplateCodesForAction(action, pkDemande);
		String bodyTemplateCode = templateCodes.getKey();
		String subjectTemplateCode = templateCodes.getValue();

		Map<String, Object> bpmVariables = gouvBPM.getProcessBusinessVariables(pkDemande);

		DemandeDTO demande = demandesService.getDemande(pkDemande);

		Map<String, Object> model = mailTemplateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode, demande,
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
	public ModelAndView mailpreview(@Valid @RequestBody PreviewFormBean mailPreviewFormBean) throws Exception {
		String action = mailPreviewFormBean.getAction();
		String codeMotifChoisi = mailPreviewFormBean.getCodeMotifChoisi();
		Integer pkDemande = mailPreviewFormBean.getPkDemande();
		String commentaire = mailPreviewFormBean.getCommentaire();
		String safeAction = AfBackUtils.logSafe(action);
		String safeCodeMotifChoisi = AfBackUtils.logSafe(codeMotifChoisi);
		String safeCommentaire = AfBackUtils.logSafe(commentaire);
		LOGGER.info("======================= Appel de /ws/mailpreview ({}, {}, {}, {})", safeAction, safeCodeMotifChoisi,
				pkDemande, safeCommentaire);
		ModelAndView mav = buildMailPreview(action, codeMotifChoisi, pkDemande, commentaire);
		LOGGER.info("======================= Fin /ws/mailpreview");
		return mav;

	}

}
