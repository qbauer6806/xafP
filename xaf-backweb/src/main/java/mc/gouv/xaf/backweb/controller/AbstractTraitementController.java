package mc.gouv.xaf.backweb.controller;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.model.CommentaireInterneDTO;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.formbean.XafTraitementFormBean;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AbstractTraitementController extends AbstractController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTraitementController.class);
	
	private static final String ERROR_MESSAGES = "errorMessages";
	
	@Autowired
	private DemandesService demandesService;
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private GouvBPM gouvBPM;
	
	// Pour les informations liées à la demande
	private static final String I18N_SAUVEGARDE_SUCCESS_CODE_MESSAGE = "message.success.sauvegarde";

	private static final String REDIRECT = "redirect:";

    @Secured({ "ROLE_TRAITEMENT", "ROLE_VALIDATION", "ROLE_LECTURE" })
	@RequestMapping(value = "/infosAdministration", method = RequestMethod.POST)
	@Transactional
	public ModelAndView infosAdministration(@ModelAttribute("traitementFormBean") XafTraitementFormBean xafTraitementFormBean,
			@RequestParam(required = true) Integer pkDemande, final RedirectAttributes redirectAttributes)
			throws IOException, SAXException {

	    LOGGER.info("======================= Appel de la page /traitement/infosAdministration ({})", pkDemande);

		LOGGER.info("Appel à DEM pour stockage des observations...");
		DemandeDTO demUpd = new DemandeDTO();
		demUpd.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
		demUpd.setPkDemandes(pkDemande);
		demUpd.setObservations(xafTraitementFormBean.getObservations());
		demandesService.updateDemande(demUpd, true);

		xafTraitementFormBean.setObservations(null);

		LOGGER.info("======================= Fin /traitement/infosAdministration");

		return returnSuccessMessage(pkDemande, I18N_SAUVEGARDE_SUCCESS_CODE_MESSAGE, redirectAttributes);
	}

	@Secured({ "ROLE_TRAITEMENT", "ROLE_VALIDATION", "ROLE_LECTURE" })
	@ResponseBody
	@RequestMapping(value = "/commentaires", method = RequestMethod.POST)
	@Transactional
	public CommentaireInterneDTO sauvegarderComm(
			@ModelAttribute("traitementFormBean") XafTraitementFormBean xafTraitementFormBean,
			@RequestParam(required = true) Integer pkDemande) throws Exception {

	    LOGGER.info("======================= Appel de la page /traitement/commentaires action=Ajouter ({})", pkDemande);

		String commString = xafTraitementFormBean.getCommentaireInterne();
		CommentaireInterneDTO commInterne = new CommentaireInterneDTO();
		if (!StringUtils.isBlank(commString)) {
			LOGGER.info("Commentaire : {}", commString);
			commInterne.setAgentId(AfBackUtils.getAuthenticatedAgentId());
			commInterne.setDate(new Date());
			commInterne.setCommentaire(commString);
			gouvBPM.putCommentaireInterne(pkDemande, commInterne);

		} else {
			throw new Exception("Impossible d'insérer un commentaire vide");
		}

		LOGGER.info("======================= Fin /traitement/commentaires action=Ajouter");

		return commInterne;
	}
	
	protected ModelAndView returnSuccessMessage(Integer pkDemande, String messageCode,
			final RedirectAttributes redirectAttributes) {
		List<String> messages = new ArrayList<>();
		messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
		redirectAttributes.addFlashAttribute("successMessages", messages);
		return new ModelAndView(REDIRECT + pkDemande);
	}

	protected ModelAndView returnSuccessMessage(Integer pkDemande, String messageCode, String demandeTab,
											  final RedirectAttributes redirectAttributes) {
		List<String> messages = new ArrayList<>();
		messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
		redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
		String url = StringUtils.isBlank(demandeTab) ? REDIRECT + pkDemande
				: REDIRECT + pkDemande + "?demandeTab=" + demandeTab;
		return new ModelAndView(url);
	}
	
	protected ModelAndView returnErrorMessage(Integer pkDemande, String messageCode,
			final RedirectAttributes redirectAttributes) {
		List<String> messages = new ArrayList<>();
		messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
		redirectAttributes.addFlashAttribute(ERROR_MESSAGES, messages);
		return new ModelAndView(REDIRECT + pkDemande);
	}

	protected ModelAndView returnErrorMessage(Integer pkDemande, String messageCode, String demandeTab,
											final RedirectAttributes redirectAttributes) {
		List<String> messages = new ArrayList<>();
		messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
		redirectAttributes.addFlashAttribute(ERROR_MESSAGES, messages);
		String url = StringUtils.isBlank(demandeTab) ? REDIRECT + pkDemande
				: REDIRECT + pkDemande + "?demandeTab=" + demandeTab;
		return new ModelAndView(url);
	}

	protected ModelAndView returnErrorMessageWithArgs(Integer pkDemande, String messageCode,
			final RedirectAttributes redirectAttributes, Object[] args) {
		List<String> messages = new ArrayList<>();
		messages.add(messageSource.getMessage(messageCode, args, Locale.FRENCH));
		redirectAttributes.addFlashAttribute(ERROR_MESSAGES, messages);
		return new ModelAndView(REDIRECT + pkDemande);
	}
	
	/**
	 * Vérifie que la soumission de la tache demandée est bien toujours la bonne
	 * dans le BPM
	 * 
	 */
	protected ModelAndView checkActiveTask(Integer pkDemande, GouvBPMTask activeTask, String activeTaskDefinitionKey,
			String messageCode, final RedirectAttributes redirectAttributes) {

		LOGGER.info("Vérification {} = {}", activeTaskDefinitionKey, activeTask.getTaskDefinitionKey());
		// Si l'active n'est plus la bonne souhaitée
		if (!StringUtils.equals(activeTaskDefinitionKey, activeTask.getTaskDefinitionKey())) {
			return returnErrorMessage(pkDemande, messageCode, redirectAttributes);

		}
		return null;
	}

	public ModelAndView form(ModelAndView mav, DemandeDTO demande, StatutPublicOuInterneDTO statutPublicOuInterne) {
		XafTraitementFormBean xafTraitementFormBean = new XafTraitementFormBean();
		xafTraitementFormBean.setObservations(demande.getObservations());
		mav.addObject("xafTraitementFormBean", xafTraitementFormBean);
		return mav;
	}
}
