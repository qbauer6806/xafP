#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.backserver.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ${groupId}.backserver.formbean.InformationsDetachementFormBean;
import ${groupId}.backserver.formbean.TraitementFormBean;
import ${groupId}.backserver.util.StateManagerUtil;
import ${groupId}.backserver.util.TraitementUtil;
import ${groupId}.service.${artifactIdCamelCase}ApiService;
import ${groupId}.service.${artifactIdCamelCase}DataService;
import ${groupId}.service.HistoService;
import ${groupId}.shared.dto.${artifactIdCamelCase}DemandeHistoriqueDTO;
import ${groupId}.shared.dto.InformationsDetachementDTO;
import ${groupId}.shared.enums.${artifactIdCamelCase}DemandeStatutEnum;
import ${groupId}.shared.model.v1573825612706.ContenuProjectDemandeDTO;
import ${groupId}.shared.model.v1573825612706.OuinonEnum;
import ${groupId}.shared.model.v1573825612706.PaysOrigineDetachementEnum;
import ${groupId}.shared.util.${artifactIdCamelCase}Utils;
import mc.gouv.logon.shared.User;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.CommentaireInterneDTO;
import mc.gouv.xaf.back.bpm.model.GouvBPMStatutAction;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemandeFilesCategorizer;
import mc.gouv.xaf.back.service.data.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.rest.PaysCache;
import mc.gouv.xaf.back.service.motifs.MotifTemplateService;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemandesComplementsComparator;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import mc.gouv.xaf.shared.dto.FileCategoryDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.ws.FileController;

/**
 * Controller pour la page /traitement
 * 
 * @author mpavone
 * 
 */
@Controller
@RequestMapping("/demandes")
public class TraitementController extends AbstractController {

	// Actions de traitement
	private static final String TRAITER = "traiter";

	// Les messages d'erreurs externalisés

	private static final String I18N_TRAITEMENT_CONCURRENT_PRIS_EN_CHARGE_ERROR_CODE_MESSAGE = "message.error.traitement.concurrent.priseencharge";
	private static final String I18N_TRAITEMENT_CONCURRENT_DEPOTIC_ERROR_CODE_MESSAGE = "message.error.traitement.concurrent.depotIC";
	private static final String I18N_TRAITEMENT_CONCURRENT_TRAIT_ERROR_CODE_MESSAGE = "message.error.traitement.concurrent.traitement";
	private static final String I18N_TRAITEMENT_CONCURRENT_FINAL_ERROR_CODE_MESSAGE = "message.error.traitement.concurrent.final";
	private static final String I18N_TRAITEMENT_CODEMOTIF_ABSENT_ERROR_CODE_MESSAGE = "message.error.traitement.codemotif.absent";

	// Les messages en cas de success externalisés

	private static final String I18N_TRAITEMENT_SUCCESS_CODE_MESSAGE = "message.success.traitement";
	private static final String I18N_ANNULATION_SUCCESS_CODE_MESSAGE = "message.success.annulation";

	// Pour les informations liées à la demande
	private static final String I18N_SAUVEGARDE_SUCCESS_CODE_MESSAGE = "message.success.sauvegarde";
	// Pour les informations liées à la demande (demande IC et finaliser)
	private static final String I18N_ENVOI_SUCCESS_CODE_MESSAGE = "message.success.envoi";
	private static final Logger LOGGER = LoggerFactory.getLogger(TraitementController.class);
	@Autowired
	private AfBackUtils afBackUtils;
	@Autowired
	private GouvBPM gouvBPM;
	@Autowired
	private MotifsCache motifsCache;
	@Autowired
	private UtilisateursCache utilisateursCache;
	@Autowired
	private PaysCache paysCache;
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;
	@Autowired
	private FileController fileController;
	@Autowired
	private ${artifactIdCamelCase}ApiService ${artifactIdLower}ApiService;
	@Autowired
	private HistoService histoService;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private DemandesService demandesService;
	@Autowired
	private DemandesHistoriqueService demandesHistoriqueService;
	@Autowired
	private DemandesStatutsService demandesStatutsService;
	@Autowired
	private DemandeFilesCategorizer demandeFilesCategorizer;
	@Autowired
	private MotifTemplateService motifTemplateService;
	@Autowired
	private ${artifactIdCamelCase}DataService ${artifactIdLower}DataService;

	@RequestMapping(value = "/{demandeId}", method = RequestMethod.GET)
	@Transactional
	public ModelAndView form(@PathVariable(value = "demandeId") Integer demandeId,
			@ModelAttribute("traitementFormBean") TraitementFormBean traitementFormBean,
			@RequestParam(name = "validationModifier", required = false) boolean validationModifier,
			@RequestParam(name = "origin", required = false) String origin, final RedirectAttributes redirectAttributes)
			throws Exception {

		LOGGER.info("======================= Appel de la page /traitement (DemandeID = {})", demandeId);
		LOGGER.info("Appel à DEM pour récupération de la demande...");

		DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);

		LOGGER.info("Contenu = {}", demande.getContenu().toString());

		StatutPublicOuInterneDTO statutPublicOuInterne = afBackUtils.getStatutPublicOuInterne(demande);

		ObjectMapper mapper = new ObjectMapper();

		// Charger certaines informations dans le formulaire si la demande est déjà en
		// cours de traitement
		if (demande.getDernierStatut().getLibelle().equals(${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name())) {
			// Commentaire usager
			Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(demande.getPkDemandes());
			if (variables != null) {
				traitementFormBean.setCommentaireUsager(
						(String) variables.get(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name()));
				traitementFormBean.setTexteAEnvoyer(
						(String) variables.get(GouvBPMProcessVariableTypeEnum.MC_TEXTE_A_ENVOYER.name()));
				// Code motif
				String codeMotif = (String) variables.get(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
				traitementFormBean.setCodeMotifChoisi(codeMotif);

				String targetState = (String) variables.get(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name());
				if (!StringUtils.isBlank(targetState)) {
					// Conversion en DemandeStatutEnum pour s'assurer de l'existence de ce statut
					// public
					${artifactIdCamelCase}DemandeStatutEnum targetState0 = ${artifactIdCamelCase}DemandeStatutEnum.valueOf(targetState);
					traitementFormBean.setStatutChoisi(targetState0.name());
				}
			}
		}

		// Si pas d'instance de process en cours pour la demande, cela veut dire que
		// celle-ci est terminé
		boolean isTerminee = !gouvBPM.isProcessInstanceAlive(demandeId);

		// Initialiser le select des motifs par rapport à l'action choisie
		List<MotifDTO> motifsInit = null;
		if (traitementFormBean.getStatutChoisi() != null) {
			motifsInit = motifTemplateService.getMotifs(demande, "fr", traitementFormBean.getStatutChoisi());
		}

		traitementFormBean.setObservations(demande.getObservations());

		// Chargement de l'historique de la demande
		// demandeId);
		List<DemandeHistoriqueDTO> histosDem = demandesHistoriqueService
				.getHistorique(gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes());

		List<${artifactIdCamelCase}DemandeHistoriqueDTO> histos${artifactIdCamelCase} = ${artifactIdCamelCase}Utils.histoDem2${artifactIdCamelCase}(histosDem);

		Integer icId = null;
		if (statutPublicOuInterne.getName().equals(${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name())) {
			// Si on est en attente d'informations complémentaires ou en acceptée sous
			// réserve, donner à la page le
			// icId
			// de la demande d'IC la plus récente
			DemandeComplementsDTO latestCompl = null;
			for (DemandeComplementsDTO compl : demande.getComplements()) {
				if (latestCompl == null || latestCompl.getQuestion().getDate().before(compl.getQuestion().getDate())) {
					latestCompl = compl;
				}
			}
			icId = latestCompl.getPkDemandeComplements();
		}

		ContenuProjectDemandeDTO contenuDemande = ${artifactIdCamelCase}Utils.getContenuDemande(demande);

		// Définition des actions disponibles sur la page de traitement pour une demande
		// en cours de traitement
		// Définir pour la page, les actions disponibles et leurs statuts cibles
		// associés, si on est à une étape de
		// choix
		List<GouvBPMStatutAction> actionsDisponibles = new ArrayList<>();
		// Extraire ces actions disponibles du diagramme BPM
		List<GouvBPMTask> activeTasks = gouvBPM.getActiveTasksForDemande(demandeId);
		if (activeTasks != null && !activeTasks.isEmpty()) {
			GouvBPMTask currentTask = gouvBPM.getActiveTasksForDemande(demandeId).get(0);
			actionsDisponibles = gouvBPM.getTaskStatutActions(currentTask);
		}

		ModelAndView mav = new ModelAndView("demandes/traitement");

		// Ajout de la tache active
		if (activeTasks != null && !activeTasks.isEmpty()) {
			GouvBPMTask activeTask = activeTasks.get(0);
			mav.addObject("activeTaskDefinitionKey", activeTask.getTaskDefinitionKey());
		}

		// Tri des demandes d'informations complémentaires par date
		if (demande.getComplements() != null) {
			Arrays.sort(demande.getComplements(), new DemandesComplementsComparator());
		}

		mav.addObject("MotifsCache", motifsCache);
		mav.addObject("PaysCache", paysCache);

		mav.addObject(statutPublicOuInterne);
		mav.addObject(demande);
		mav.addObject(contenuDemande);

		mav.addObject("motifsInit", motifsInit);

		/** Section Informations detachement **/

		InformationsDetachementDTO informationsDetachementDTO = getCalculeAideDTO(demandeId);
		mav.addObject(TraitementUtil.mapInformationDetachementDTO2FormBean(informationsDetachementDTO));

		mav.addObject("isInformationsDetachementPanelActive",
				StateManagerUtil.isInformationsDetachementPanelActive(statutPublicOuInterne));

		mav.addObject("isInformationsDetachementAccardeonIsOpen",
				StateManagerUtil.isInformationsDetachementPanelActive(statutPublicOuInterne));


		/** Section control de visibilité **/

		mav.addObject("isEnAttentTraitemant",
				statutPublicOuInterne.getName().equals(${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.name()));
		mav.addObject("isEnCoursTraitement",
				statutPublicOuInterne.getName().equals(${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name()));
		mav.addObject("isEnValidee", statutPublicOuInterne.getName().equals(${artifactIdCamelCase}DemandeStatutEnum.VALIDEE.name()));
		mav.addObject("isValidationActive", true);
		mav.addObject("isRefuseActive", StateManagerUtil.isRefuseActive(statutPublicOuInterne, demande));
		mav.addObject("isComplementActive", StateManagerUtil.isComplementActive(statutPublicOuInterne, demande));
		mav.addObject("isAnnulationActive", StateManagerUtil.isAnnulationActive(statutPublicOuInterne, demande));
		mav.addObject("isMotifActive", StateManagerUtil.isMotifActive(statutPublicOuInterne, demande));
		mav.addObject("isTraitementVisible", StateManagerUtil.isTraitementVisible(statutPublicOuInterne, demande));
		mav.addObject("isReprendreEnchargeVisible",
				StateManagerUtil.isReprendreEnchargeVisible(statutPublicOuInterne, demande));
		mav.addObject("isObservationPanelActive", StateManagerUtil.isObservationPanelActive(statutPublicOuInterne));
		mav.addObject("isDiscussionPanelActive", StateManagerUtil.isDiscussionPanelActive(statutPublicOuInterne));
		mav.addObject("hasRoleValidationOrTraitement", StateManagerUtil.hasRoleValidationOrTraitement());

		/** FIN de Section control de visibilité **/

		String utilisateurAffecte = StringUtils.EMPTY;

		if (StringUtils.isNotBlank(demande.getAgentAffecteId())) {
			User u = utilisateursCache.get(demande.getAgentAffecteId());
			if (u != null) {
				utilisateurAffecte = u.getNom();
			}

		}

		mav.addObject("utilisateurAffecte", utilisateurAffecte);

		String utilisateurConnecte = AfBackUtils.getAuthenticatedAgentName();

		mav.addObject("utilisateurConnecte", utilisateurConnecte);

		mav.addObject("isTerminee", isTerminee);
		// Pour le script JS de mise à jour du select des motifs
		boolean isChantier = contenuDemande.getDonnee().getEntreprise().getEstchantier().equals(OuinonEnum.YES);
		boolean isFrance = contenuDemande.getDonnee().getEntreprise().getPaysoriginedetachement().equals(PaysOrigineDetachementEnum.FR);
		List<MotifDTO> motifsJS = motifTemplateService.getFilteredMotifs(demande, "fr", TraitementUtil.getMotifsAAfficher(isChantier, isFrance));

		boolean containsDTO = false;
		boolean condition = demande.getDernierStatut() != null && demande.getDernierStatut().getCodeMotif() != null;

		if (condition && motifsInit != null) {
			for (MotifDTO motifSearch : motifsInit) {
				if (motifSearch.getCode().equals(demande.getDernierStatut().getCodeMotif())) {
					containsDTO = true;
				}
			}
		}

		// Vérification ou ajout à la main du motif dans la liste si celui-ci a été
		// sélectionné mais ensuite
		// désactivé
		if (condition && !containsDTO && motifsInit != null) {

			MotifDTO motifDesactive = motifTemplateService.getMotif(demande, demande.getDernierStatut().getCodeMotif(),
					"fr");
			motifsInit.add(motifDesactive);
			motifsJS.add(motifDesactive);
		}

		containsDTO = false;
		condition = traitementFormBean.getCodeMotifChoisi() != null;

		if (condition && motifsJS != null) {
			for (MotifDTO motifSearch : motifsJS) {
				if (motifSearch.getCode().equals(traitementFormBean.getCodeMotifChoisi())) {
					containsDTO = true;
				}
			}
		}

		if (condition && !containsDTO && motifsInit != null) {
			MotifDTO motifDesactive = motifTemplateService.getMotif(demande, traitementFormBean.getCodeMotifChoisi(),
					"fr");
			motifsInit.add(motifDesactive);
			motifsJS.add(motifDesactive);
		}

		mav.addObject("motifs", mapper.writeValueAsString(motifsJS));
		mav.addObject("histos", histos${artifactIdCamelCase});

		List<FileCategoryDTO> categories = demandeFilesCategorizer.getCategoriesAndFiles(demande);
		mav.addObject("categories", categories);

		int fileCount = 0;
		for (FileCategoryDTO cat : categories) {
			if (cat.getFiles() != null) {
				fileCount += cat.getFiles().size();
			}
		}
		mav.addObject("fileCount", fileCount);

		if (icId != null) {
			mav.addObject("icId", icId);
		}

		List<CommentaireInterneDTO> commInternes = gouvBPM.getCommentairesInternes(demandeId);
		mav.addObject("commInternes", commInternes);
		mav.addObject("actionsDisponibles", actionsDisponibles);

		if (${artifactIdCamelCase}DemandeStatutEnum.VALIDEE.name().equals(demande.getDernierStatut().getLibelle())
				|| ${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name().equals(demande.getDernierStatut().getLibelle())
				|| ${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name().equals(demande.getDernierStatut().getLibelle())) {
			mav.addObject("accesDesactive",
					demandesService.isAccesDesactive(gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes()));
		}

		if ("fo".equals(origin)) {
			List<String> messages = new ArrayList<>();
			redirectAttributes.addFlashAttribute("successMessages", messages);
			return new ModelAndView("redirect:" + demandeId);
		}

		LOGGER.info("======================= Fin /traitement");

		return mav;
	}

	private InformationsDetachementDTO getCalculeAideDTO(Integer demandeID) {
		return ${artifactIdLower}DataService.getInformationsDetachement(demandeID);
	}

	@Secured("ROLE_TRAITEMENT")
	@RequestMapping(value = "/informationsdetachement", method = RequestMethod.POST)
	@Transactional
	public ModelAndView informationDetachement(@Valid @ModelAttribute("informationsDetachementFormBean") InformationsDetachementFormBean informationsDetachementFormBean,
								   @RequestParam(required = true) Integer pkDemande, final RedirectAttributes redirectAttributes,
								   BindingResult bindingResult) throws Exception {

		if (bindingResult.hasErrors()) {
			throw new Exception(bindingResult.getAllErrors().get(0).getDefaultMessage());
		}
		${artifactIdLower}DataService.saveInformationsDetachementDTO(TraitementUtil.mapInformationDetachementFormToDTO(informationsDetachementFormBean), pkDemande);

		return returnSuccessMessage(pkDemande, I18N_SAUVEGARDE_SUCCESS_CODE_MESSAGE, redirectAttributes);
	}

	/**
	 * Vérifie que la soumission de la tache demandée est bien toujours la bonne
	 * dans le BPM
	 * 
	 */
	private ModelAndView checkActiveTask(Integer pkDemande, GouvBPMTask activeTask, String activeTaskDefinitionKey,
			String messageCode, final RedirectAttributes redirectAttributes) {

		LOGGER.info("Vérification {} = {}", activeTaskDefinitionKey, activeTask.getTaskDefinitionKey());
		// Si l'active n'est plus la bonne souhaitée
		if (!StringUtils.equals(activeTaskDefinitionKey, activeTask.getTaskDefinitionKey())) {
			return returnErrorMessage(pkDemande, messageCode, redirectAttributes);

		}
		return null;
	}

	@Secured("ROLE_TRAITEMENT")
	@RequestMapping(value = "/prendreEnCharge", method = RequestMethod.POST)
	@Transactional
	public ModelAndView prendreEnCharge(@RequestParam(required = true) Integer pkDemande,
			@RequestParam(required = true) String activeTaskDefinitionKey, final RedirectAttributes redirectAttributes)
			throws Exception {

		LOGGER.info("======================= Appel de la page /traitement/prendreEnCharge ({})", pkDemande);

		List<GouvBPMTask> activeTasks = gouvBPM.getActiveTasksForDemande(pkDemande);
		if (activeTasks == null || (activeTasks != null && activeTasks.isEmpty())) {
			return returnErrorMessage(pkDemande, I18N_TRAITEMENT_CONCURRENT_FINAL_ERROR_CODE_MESSAGE,
					redirectAttributes);
		}
		GouvBPMTask activeTask = activeTasks.get(0);

		ModelAndView mav = checkActiveTask(pkDemande, activeTask, activeTaskDefinitionKey,
				I18N_TRAITEMENT_CONCURRENT_PRIS_EN_CHARGE_ERROR_CODE_MESSAGE, redirectAttributes);
		if (mav != null) {
			return mav;
		}
		LOGGER.info("claimTask() puis submitTaskFormData()...");
		GouvBPMUser user = new GouvBPMUser();
		String userId = AfBackUtils.getAuthenticatedAgentId();
		user.setId(userId);
		gouvBPM.setAssignee(pkDemande, userId);

		gouvBPM.claimTask(activeTask, user);
		gouvBPM.submitTaskFormData(activeTask, null, pkDemande);

		// Mettre à jour l'agent affecté dans DEM
		LOGGER.info("Appel de DEM pour définir l'agent affectué...");
		DemandeDTO demUpd = new DemandeDTO();
		demUpd.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
		demUpd.setPkDemandes(pkDemande);
		demUpd.setAgentAffecteId(userId);
		demandesService.updateDemande(demUpd, true);

		// Ajout d'une ligne à l'historique
		DemandeHistoriqueDTO histo = histoService.prendreEnCharge(pkDemande,
				${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name(), AfBackUtils.getAuthenticatedAgentId());
		LOGGER.info("Appel à DEM pour historique...");
		try {
			demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(), pkDemande, histo);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
		}

		mav = new ModelAndView("redirect:" + pkDemande);

		LOGGER.info("======================= Fin /traitement/prendreEnCharge");

		return mav;
	}

	@Secured("ROLE_TRAITEMENT")
	@RequestMapping(value = "/annuler", method = RequestMethod.POST)
	@Transactional
	public ModelAndView annuler(@RequestParam(required = true) Integer pkDemande,
			@RequestParam(required = true) String commentaire, @RequestParam(required = true) String codeMotif,
			final RedirectAttributes redirectAttributes) throws Exception {

	    LOGGER.info("======================= Appel de la page /traitement/Annuler ({})", pkDemande);

		GouvBPMUser agent = new GouvBPMUser();
		agent.setId(AfBackUtils.getAuthenticatedAgentId());

		Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(pkDemande);
		variables.put(GouvBPMProcessVariableTypeEnum.MC_ANNULATION_ORIGINATOR_USAGER.name(), null);
		gouvBPM.setProcessBusinessVariables(pkDemande, variables);

		gouvBPM.annulerDemande(pkDemande, agent, null, codeMotif, commentaire, ${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name());

		DemandeHistoriqueDTO histo = histoService.statusChange(pkDemande, ${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name(), null,
				null, AfBackUtils.getAuthenticatedAgentId());
		LOGGER.info("Appel à DEM pour historique...");
		try {
			demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(), pkDemande, histo);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
		}

		LOGGER.info("======================= Fin /traitement/prendreEnCharge");

		return returnSuccessMessage(pkDemande, I18N_ANNULATION_SUCCESS_CODE_MESSAGE, redirectAttributes);
	}

	@Secured("ROLE_TRAITEMENT")
	@RequestMapping(value = "/reprendreEnCharge", method = RequestMethod.POST)
	@Transactional
	public ModelAndView reprendreEnCharge(@RequestParam(required = true) Integer pkDemande) throws Exception {

		LOGGER.info("======================= Appel de la page /traitement/reprendreEnCharge ({})", pkDemande);
		LOGGER.info("Affecter la demande à l'agent connecté (appel à DEM)...");

		reprendreEnCharge(pkDemande, AfBackUtils.getAuthenticatedAgentId());

		ModelAndView mav = new ModelAndView("redirect:" + pkDemande);

		LOGGER.info("======================= Fin /traitement/reprendreEnCharge");

		return mav;
	}

	private void reprendreEnCharge(Integer pkDemande, String agentId) throws IOException, SAXException {

		LOGGER.info("Affecter la demande à l'agent connecté (appel à DEM)...");

		GouvBPMUser user = new GouvBPMUser();
		String userId = agentId;
		user.setId(userId);
		// On change l'assignee
		gouvBPM.setAssignee(pkDemande, userId);

		DemandeDTO demUpd = new DemandeDTO();
		demUpd.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
		demUpd.setPkDemandes(pkDemande);
		demUpd.setAgentAffecteId(agentId);
		DemandeDTO demande = demandesService.updateDemande(demUpd, true);

		// Ajout d'une ligne à l'historique

		DemandeHistoriqueDTO histo = histoService.prendreEnCharge(pkDemande, demande.getDernierStatut().getLibelle(),
				agentId);
		LOGGER.info("Appel à DEM pour historique...");
		try {
			demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(), pkDemande, histo);

		} catch (Exception e) {
			LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
		}

	}

	private ModelAndView returnErrorMessage(Integer pkDemande, String messageCode,
			final RedirectAttributes redirectAttributes) {
		List<String> messages = new ArrayList<>();
		messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
		redirectAttributes.addFlashAttribute("errorMessages", messages);
		return new ModelAndView("redirect:" + pkDemande);
	}

	private ModelAndView returnSuccessMessage(Integer pkDemande, String messageCode,
			final RedirectAttributes redirectAttributes) {
		List<String> messages = new ArrayList<>();
		messages.add(messageSource.getMessage(messageCode, null, Locale.FRENCH));
		redirectAttributes.addFlashAttribute("successMessages", messages);
		return new ModelAndView("redirect:" + pkDemande);
	}

	@Secured("ROLE_TRAITEMENT")
	@RequestMapping(value = "/traiter", method = RequestMethod.POST, params = "action=Traiter")
	@Transactional
	public ModelAndView traiter(@ModelAttribute("traitementFormBean") TraitementFormBean traitementFormBean,
			@RequestParam(required = true) Integer pkDemande, final RedirectAttributes redirectAttributes)
			throws Exception {

	    LOGGER.info("======================= Appel de la page /traitement/traiter action=Traiter ({})", pkDemande);

		${artifactIdCamelCase}DemandeStatutEnum targetState = ${artifactIdCamelCase}DemandeStatutEnum.valueOf(traitementFormBean.getStatutChoisi());

		ModelAndView mav = traiterGeneric(traitementFormBean, pkDemande, redirectAttributes, TRAITER);
		if (mav != null) {
			return mav;
		}

		DemandeHistoriqueDTO histo = null;
		// Ajout d'une ligne à l'historique
		if (${artifactIdCamelCase}DemandeStatutEnum.VALIDEE == targetState) {
			histo = histoService.traiterFinal(pkDemande, targetState.name(), AfBackUtils.getAuthenticatedAgentId());
		} else if (${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL == targetState) {
			histo = histoService.statusChange(pkDemande, ${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name(), null, null,
					AfBackUtils.getAuthenticatedAgentId());
		} else if (${artifactIdCamelCase}DemandeStatutEnum.REFUSEE == targetState) {
			histo = histoService.statusChange(pkDemande, ${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name(), null, null,
					AfBackUtils.getAuthenticatedAgentId());
		}

		LOGGER.info("Appel à DEM pour historique...");
		try {
			demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(), pkDemande, histo);

		} catch (Exception e) {
			LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
		}

		LOGGER.info("======================= Fin /traitement/traiter action=Traiter");

		return returnSuccessMessage(pkDemande, I18N_TRAITEMENT_SUCCESS_CODE_MESSAGE, redirectAttributes);
	}

	/**
	 * Retourne un ModelAndView en cas d'erreur
	 * 
	 * @param traitementFormBean
	 * @param pkDemande
	 * @param redirectAttributes
	 * @return
	 * @throws Exception
	 */
	private ModelAndView traiterGeneric(@ModelAttribute("traitementFormBean") TraitementFormBean traitementFormBean,
			@RequestParam(required = true) Integer pkDemande, final RedirectAttributes redirectAttributes,
			String action) throws Exception {

		LOGGER.info("Statut choisi : " + traitementFormBean.getStatutChoisi());
		LOGGER.info("Code motif choisi : " + traitementFormBean.getCodeMotifChoisi());
		LOGGER.info("Commentaire usager : " + traitementFormBean.getCommentaireUsager());
		LOGGER.info("Texte à envoyer à l'usager : " + traitementFormBean.getTexteAEnvoyer());

		if (!StringUtils.startsWith(traitementFormBean.getStatutChoisi(), ${artifactIdCamelCase}DemandeStatutEnum.VALIDEE.name())
				&& StringUtils.isBlank(traitementFormBean.getCodeMotifChoisi())) {
			return returnErrorMessage(pkDemande, I18N_TRAITEMENT_CODEMOTIF_ABSENT_ERROR_CODE_MESSAGE,
					redirectAttributes);
		}

		// Gestion pour voir si la tache courante est bien Validation Hiérarchique
		List<GouvBPMTask> activeTasks = gouvBPM.getActiveTasksForDemande(pkDemande);
		if (activeTasks == null || (activeTasks != null && activeTasks.isEmpty())) {
			return returnErrorMessage(pkDemande, I18N_TRAITEMENT_CONCURRENT_FINAL_ERROR_CODE_MESSAGE,
					redirectAttributes);
		}
		GouvBPMTask activeTask = activeTasks.get(0);

		ModelAndView mav = checkActiveTask(pkDemande, activeTask, traitementFormBean.getActiveTaskDefinitionKey(),
				I18N_TRAITEMENT_CONCURRENT_TRAIT_ERROR_CODE_MESSAGE, redirectAttributes);
		if (mav != null) {
			return mav;
		}

		LOGGER.info(
				"Stockage dans le process du statut cible, du code motif choisi, ainsi que du commentaire usager...");
		String targetStateStr = traitementFormBean.getStatutChoisi();
		String codeMotifChoisi = traitementFormBean.getCodeMotifChoisi();
		String commentaireUsager = traitementFormBean.getCommentaireUsager();
		String texteAEnvoyer = traitementFormBean.getTexteAEnvoyer();

		// Pas de code motif ni commentaire si statut cible VALIDEE
		if (${artifactIdCamelCase}DemandeStatutEnum.VALIDEE.name().equals(targetStateStr)) {
			traitementFormBean.setCodeMotifChoisi(null);
			traitementFormBean.setCommentaireUsager(null);
			traitementFormBean.setTexteAEnvoyer(null);
			// Si on est en validation hiérarchique, on retire le motif et le commentaire de
			// BPMN
			if (!TRAITER.equals(action)) {
				gouvBPM.removeProcessBusinessVariables(pkDemande,
						GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
				gouvBPM.removeProcessBusinessVariables(pkDemande,
						GouvBPMProcessVariableTypeEnum.MC_TEXTE_A_ENVOYER.name());
				gouvBPM.removeProcessBusinessVariables(pkDemande, GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
			}
		}

		Map<String, String> formData = new HashMap<>();
		${artifactIdCamelCase}DemandeStatutEnum targetState = ${artifactIdCamelCase}DemandeStatutEnum.valueOf(targetStateStr);
		GouvBPMTask task = gouvBPM.getActiveTasksForDemande(pkDemande).get(0);

		LOGGER.info("claimTask() puis submitTaskFormData()...");
		GouvBPMUser user = new GouvBPMUser();
		user.setId(AfBackUtils.getAuthenticatedAgentId());

		mav = claimTask(pkDemande, task, user, redirectAttributes);
		if (mav != null) {
			return mav;
		}

		formData.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name(), targetState.name());
		if (!StringUtils.isBlank(codeMotifChoisi)) {
			formData.put(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name(), codeMotifChoisi);
		}
		if (!StringUtils.isBlank(commentaireUsager)) {
			formData.put(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name(), commentaireUsager);
		}
		if (!StringUtils.isBlank(texteAEnvoyer)) {
			formData.put(GouvBPMProcessVariableTypeEnum.MC_TEXTE_A_ENVOYER.name(), texteAEnvoyer);
		}
		gouvBPM.submitTaskFormData(task, formData, pkDemande);

		gouvBPM.removeProcessBusinessVariables(pkDemande, GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name());
		gouvBPM.removeProcessBusinessVariables(pkDemande, GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
		gouvBPM.removeProcessBusinessVariables(pkDemande, GouvBPMProcessVariableTypeEnum.MC_TEXTE_A_ENVOYER.name());
		gouvBPM.removeProcessBusinessVariables(pkDemande, GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());

		LOGGER.info("======================= Fin /traitement/traiter action=Traiter");

		return null;

	}

	/**
	 * Claim la tache et vérifie qu'il y a pas d'erreur de concurrence Si c'est le
	 * cas , retourne le modelAndView
	 * 
	 * @param task
	 * @param user
	 * @return
	 */
	private ModelAndView claimTask(Integer pkDemande, GouvBPMTask task, GouvBPMUser user,
			final RedirectAttributes redirectAttributes) {
		try {
			gouvBPM.claimTask(task, user);
		} catch (TaskAlreadyClaimedException e1) {
		    LOGGER.error("La tâche {} ({}) est déjà attribuée !", task.getId(), task.getTaskDefinitionKey());
			return returnErrorMessage(pkDemande, I18N_TRAITEMENT_CONCURRENT_PRIS_EN_CHARGE_ERROR_CODE_MESSAGE,
					redirectAttributes);
		}
		return null;
	}

	/**
	 * Plus utilisé normalement Je le laisse pou le moment s'ils veulent revenir
	 * dessus.
	 * 
	 * @param traitementFormBean
	 * @param pkDemande
	 * @return
	 */
	@Secured("ROLE_TRAITEMENT")
	@RequestMapping(value = "/traiter", method = RequestMethod.POST, params = "action=Sauvegarder")
	@Transactional
	public ModelAndView sauvegarder(@ModelAttribute("traitementFormBean") TraitementFormBean traitementFormBean,
			@RequestParam(required = true) Integer pkDemande, final RedirectAttributes redirectAttributes)
			throws IOException, SAXException {

	    LOGGER.info("======================= Appel de la page /traitement/traiter action=Sauvegarder ({})", pkDemande);
        LOGGER.info("Action choisie : {}", traitementFormBean.getStatutChoisi());
        LOGGER.info("Code motif choisi : {}", traitementFormBean.getCodeMotifChoisi());
        LOGGER.info("Commentaire usager : {}", traitementFormBean.getCommentaireUsager());
        LOGGER.info("Tetxe à envoyer à l'usager : {}", traitementFormBean.getTexteAEnvoyer());

		LOGGER.info(
				"Stockage dans le process du statut cible, du code motif choisi, ainsi que du commentaire usager...");
		Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(pkDemande);

		${artifactIdCamelCase}DemandeStatutEnum targetState = ${artifactIdCamelCase}DemandeStatutEnum.valueOf(traitementFormBean.getStatutChoisi());
		variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name(), targetState.name());
		variables.put(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name(), traitementFormBean.getCodeMotifChoisi());
		variables.put(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name(),
				traitementFormBean.getCommentaireUsager());
		variables.put(GouvBPMProcessVariableTypeEnum.MC_TEXTE_A_ENVOYER.name(),
				traitementFormBean.getTexteAEnvoyer());
		gouvBPM.setProcessBusinessVariables(pkDemande, variables);

		// Si l'agent qui sauvegarde n'est pas l'agent à qui est affectée la demande,
		// effectuer une reprise en charge
		String agentId = AfBackUtils.getAuthenticatedAgentId();
		String assignee = (String) variables.get(GouvBPMProcessVariableTypeEnum.MC_ASSIGNEE.name());
		if (!agentId.equals(assignee)) {
		    LOGGER.info("Reprendre en charge la demande, passage de l'agent {} à l'agent {}.", assignee, agentId);
			reprendreEnCharge(pkDemande, agentId);
		}

		LOGGER.info("======================= Fin /traitement/traiter action=Sauvegarder");

		return returnSuccessMessage(pkDemande, I18N_SAUVEGARDE_SUCCESS_CODE_MESSAGE, redirectAttributes);
	}

	@Secured("ROLE_TRAITEMENT")
	@RequestMapping(value = "/finaliser", method = RequestMethod.POST, params = "action=Finaliser")
	@Transactional
	public ModelAndView finaliser(@ModelAttribute("traitementFormBean") TraitementFormBean traitementFormBean,
			@RequestParam(required = true) Integer pkDemande, final RedirectAttributes redirectAttributes)
			throws Exception {

	    LOGGER.info("======================= Appel de la page /traitement/finaliser action=Finaliser ({})", pkDemande);
        LOGGER.info("Statut choisi : {}", traitementFormBean.getStatutChoisi());
        LOGGER.info("Code motif choisi : {}", traitementFormBean.getCodeMotifChoisi());
        LOGGER.info("Commentaire usager : {}", traitementFormBean.getCommentaireUsager());
        LOGGER.info("Texte à envoyer à l'usager : {}", traitementFormBean.getTexteAEnvoyer());

		// Gestion pour voir si la tache courante est bien Validation Hiérarchique
		List<GouvBPMTask> activeTasks = gouvBPM.getActiveTasksForDemande(pkDemande);
		if (activeTasks == null || (activeTasks != null && activeTasks.isEmpty())) {
			return returnErrorMessage(pkDemande, I18N_TRAITEMENT_CONCURRENT_FINAL_ERROR_CODE_MESSAGE,
					redirectAttributes);
		}
		GouvBPMTask activeTask = activeTasks.get(0);
		ModelAndView mav = checkActiveTask(pkDemande, activeTask, traitementFormBean.getActiveTaskDefinitionKey(),
				I18N_TRAITEMENT_CONCURRENT_FINAL_ERROR_CODE_MESSAGE, redirectAttributes);
		if (mav != null) {
			return mav;
		}

		// Indication du code motif et du commentaire s'il s'agit d'un refus
		Map<String, String> formData = new HashMap<>();
		if (!StringUtils.isBlank(traitementFormBean.getCodeMotifChoisi())) {
			formData.put(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name(), traitementFormBean.getCodeMotifChoisi());
		}
		if (!StringUtils.isBlank(traitementFormBean.getCommentaireUsager())) {
			formData.put(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name(),
					traitementFormBean.getCommentaireUsager());
		}
		if (!StringUtils.isBlank(traitementFormBean.getTexteAEnvoyer())) {
			formData.put(GouvBPMProcessVariableTypeEnum.MC_TEXTE_A_ENVOYER.name(),
					traitementFormBean.getTexteAEnvoyer());
		}
		${artifactIdCamelCase}DemandeStatutEnum targetState = ${artifactIdCamelCase}DemandeStatutEnum.valueOf(traitementFormBean.getStatutChoisi());
		formData.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name(), targetState.name());

		LOGGER.info("claimTask() puis submitTaskFormData()...");
		GouvBPMUser user = new GouvBPMUser();
		user.setId(AfBackUtils.getAuthenticatedAgentId());
		mav = claimTask(pkDemande, activeTask, user, redirectAttributes);
		if (mav != null) {
			return mav;
		}
		gouvBPM.submitTaskFormData(activeTask, formData, pkDemande);

		// Ajout d'une ligne à l'historique
		DemandeHistoriqueDTO histo = histoService.traiterFinal(pkDemande, targetState.name(),
				AfBackUtils.getAuthenticatedAgentId());
		LOGGER.info("Appel à DEM pour historique...");
		try {
			demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(), pkDemande, histo);

		} catch (Exception e) {
			LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
		}

		mav = returnSuccessMessage(pkDemande, I18N_ENVOI_SUCCESS_CODE_MESSAGE, redirectAttributes);

		LOGGER.info("======================= Fin /traitement/finaliser action=Finaliser");

		return mav;
	}

	@Secured({ "ROLE_TRAITEMENT", "ROLE_VALIDATION", "ROLE_LECTURE" })
	@RequestMapping(value = "/infosAdministration", method = RequestMethod.POST)
	@Transactional
	public ModelAndView infosAdministration(@ModelAttribute("traitementFormBean") TraitementFormBean traitementFormBean,
			@RequestParam(required = true) Integer pkDemande, final RedirectAttributes redirectAttributes)
			throws IOException, SAXException {

	    LOGGER.info("======================= Appel de la page /traitement/infosAdministration ({})", pkDemande);

		LOGGER.info("Appel à DEM pour stockage des observations...");
		DemandeDTO demUpd = new DemandeDTO();
		demUpd.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
		demUpd.setPkDemandes(pkDemande);
		demUpd.setObservations(traitementFormBean.getObservations());
		demandesService.updateDemande(demUpd, true);

		traitementFormBean.setObservations(null);

		LOGGER.info("======================= Fin /traitement/infosAdministration");

		return returnSuccessMessage(pkDemande, I18N_SAUVEGARDE_SUCCESS_CODE_MESSAGE, redirectAttributes);
	}

	@Secured({ "ROLE_TRAITEMENT", "ROLE_VALIDATION", "ROLE_LECTURE" })
	@ResponseBody
	@RequestMapping(value = "/commentaires", method = RequestMethod.POST)
	@Transactional
	public CommentaireInterneDTO sauvegarderComm(
			@ModelAttribute("traitementFormBean") TraitementFormBean traitementFormBean,
			@RequestParam(required = true) Integer pkDemande) throws Exception {

	    LOGGER.info("======================= Appel de la page /traitement/commentaires action=Ajouter ({})", pkDemande);

		String commString = traitementFormBean.getCommentaireInterne();
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

	@Secured("ROLE_TRAITEMENT")
	@PostMapping("/repondreDIC")
	@Transactional
	public ModelAndView repondreDIC(@RequestParam MultipartFile[] uploadingFiles, HttpServletResponse response,
			@RequestParam String commentaireReponse, @RequestParam(required = true) Integer pkDemande,
			@RequestParam(required = true) Integer icId, @RequestParam(required = true) String activeTaskDefinitionKey,
			final RedirectAttributes redirectAttributes) throws Exception {

		LOGGER.info("Appel de la page /traitement/repondreDIC");
		LOGGER.info("commentaireReponse = {}", commentaireReponse);

		// Gestion pour voir si la tache courante est bien depotICTask
		GouvBPMTask activeTask = gouvBPM.getActiveTasksForDemande(pkDemande).get(0);

		ModelAndView mav = checkActiveTask(pkDemande, activeTask, activeTaskDefinitionKey,
				I18N_TRAITEMENT_CONCURRENT_DEPOTIC_ERROR_CODE_MESSAGE, redirectAttributes);
		if (mav != null) {
			return mav;
		}

		LOGGER.info("étape 1 : upload des fichiers dans FILE...");
		Map<String, String> fileNames = fileController.saveFiles(pkDemande, uploadingFiles, response, pkDemande);

		LOGGER.info("étape 2 : création de la réponse dans DEM...");
		DemandeComplementsReponseDTO reponse = new DemandeComplementsReponseDTO();
		reponse.setAgentId(AfBackUtils.getAuthenticatedAgentId());
		reponse.setTexte(commentaireReponse);
		List<DemandeComplementsFileDTO> complFiles = new ArrayList<>();
		fileNames.keySet().forEach(fileName -> {
			DemandeComplementsFileDTO file = new DemandeComplementsFileDTO();
			file.setName(fileName);
			file.setUrl(fileNames.get(fileName));
			complFiles.add(file);
		});
		reponse.setFichiers(complFiles.toArray(new DemandeComplementsFileDTO[complFiles.size()]));

		${artifactIdLower}ApiService.repondreDemandeComplements(pkDemande, icId, reponse);

		LOGGER.info("======================= Fin /traitement/repondreDIC");

		mav = returnSuccessMessage(pkDemande, I18N_ENVOI_SUCCESS_CODE_MESSAGE, redirectAttributes);

		return mav;
	}

	@Secured("ROLE_TRAITEMENT")
	@RequestMapping(value = "/dupliquer", method = RequestMethod.POST)
	@Transactional
	public ModelAndView dupliquer(@RequestParam(required = true) Integer pkDemande) throws Exception {

	    LOGGER.info("======================= Appel de la page /traitement/dupliquer ({})", pkDemande);

		LOGGER.info("Appel à DEM pour dupliquer la demande... {}", pkDemande);
		DemandeDTO demandeDupliquee = null;
		String demarcheId = gouvPropertiesResolver.getDemarcheId();

		try {
			demandeDupliquee = demandesService.cloneDemande(demarcheId, pkDemande);

			LOGGER.info("Nouvelle demande : " + demandeDupliquee.getPkDemandes());

			LOGGER.info("Appel à DEM pour créer un nouveau statut ${symbol_escape}"En attente de traitement${symbol_escape}"");
			demandesStatutsService.updateStatut(gouvPropertiesResolver.getDemarcheId(),
					demandeDupliquee.getPkDemandes(), ${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.name(),
					demandeDupliquee.getAgentAffecteId(), null, null, "DUPLICATION", "DUPLICATION");

			LOGGER.info("Création d'une instance de process dans le BPM pour cette demande ({})...",
                    demandeDupliquee.getPkDemandes());
			GouvBPMUser user = new GouvBPMUser();
			user.setId(demandeDupliquee.getUsagerId().toString());

			String canal = demandeDupliquee.getCanal().name();

			// Définition des process variables
			ContenuProjectDemandeDTO contenuDemande = ${artifactIdCamelCase}Utils.getContenuDemande(demandeDupliquee);
			Map<String, Object> variables = new HashMap<>();

			variables.put(GouvBPMProcessVariableTypeEnum.MC_CONTENU_DEMANDE.name(), contenuDemande);
			variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_CANAL.name(), canal);
			variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_LANGUE.name(),
					StringUtils.lowerCase(demandeDupliquee.getLangue()));
			variables.put(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name(), demandeDupliquee.getUsagerId());
			variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_IDENTIFIANT.name(),
					demandeDupliquee.getIdentifiant());

			gouvBPM.startProcessInstanceByMessage("duplicationMessage", user, demandeDupliquee.getPkDemandes(),
					gouvPropertiesResolver.getDemarcheId(), variables);

		} catch (Exception e) {
			LOGGER.error("Erreur lors de la duplication d'une demande {}", demandeDupliquee, e);

			if (demandeDupliquee != null && demandeDupliquee.getPkDemandes() != null) {
				LOGGER.error(
						"Suppression de la demande dans DEM id:{} identifiant:{}" + demandeDupliquee.getPkDemandes(),
						demandeDupliquee.getIdentifiant());
				demandesService.deleteDemande(gouvPropertiesResolver.getDemarcheId(), demandeDupliquee.getPkDemandes());
			}

			// Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
			throw new RuntimeException("Erreur lors de la création d'une demande", e);
		}

		// Ajout d'une ligne à l'historique
		DemandeHistoriqueDTO histoNouvelleDemande = histoService.historiqueDuplicationNouvelleDemande(demandeDupliquee.getPkDemandes(), pkDemande, demarcheId,
				AfBackUtils.getAuthenticatedAgentId());
		DemandeHistoriqueDTO histoAncienneDemande = histoService.historiqueDuplicationAncienneDemande(demandeDupliquee.getPkDemandes(), pkDemande, demarcheId,
				AfBackUtils.getAuthenticatedAgentId());
		LOGGER.info("Appel à DEM pour historique...");
		try {
			demandesHistoriqueService.saveHistorique(demarcheId, demandeDupliquee.getPkDemandes(), histoNouvelleDemande);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de la création de l'historique {}", histoNouvelleDemande, e);
		}

		try {
			demandesHistoriqueService.saveHistorique(demarcheId, pkDemande, histoAncienneDemande);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de la création de l'historique {}", histoAncienneDemande, e);
		}

		ModelAndView mav = new ModelAndView("redirect:" + demandeDupliquee.getPkDemandes());

		LOGGER.info("======================= Fin /traitement/dupliquer");

		return mav;
	}

}
