package mc.gouv.xaf.back.service.purge;

import static mc.gouv.xaf.shared.enums.DemandeCanalEnum.COURRIER;
import static mc.gouv.xaf.shared.enums.DemandeCanalEnum.GUICHET_PHYSIQUE;
import static mc.gouv.xaf.shared.enums.DemandeCanalEnum.GUICHET_VIRTUEL;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.data.dao.BrouillonsFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesComplementsFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesCourriersRepository;
import mc.gouv.xaf.back.data.dao.DemandesFilesRepository;
import mc.gouv.xaf.back.data.dao.PurgeFilesRepository;
import mc.gouv.xaf.back.data.dao.StatistiquesRepository;
import mc.gouv.xaf.back.data.entity.PurgeFilesBO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.GouvSchedulerService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.enums.MailAudienceEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class PurgeDemandesServiceImpl implements PurgeDemandesService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PurgeDemandesServiceImpl.class);

	private static final Integer OFFSET_MOIS_DATE_PURGE = 1;

    private static final Integer PURGE_DEMANDES_PAR_LOT_TAILLE_FILE = 100;

	private static final String DELAI_ENVOI_MAIL_PURGE = "DELAI_ENVOI_MAIL_PURGE";

	@Autowired
	private DemandesService demandesService;
	
	@Autowired
    private DemarchesDataProvider demarchesDataProvider;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
    private EntityManager em;

	@Autowired
	private MailService mailService;

	@Autowired
	private AfBackUtils afBackUtils;

	@Autowired
    private PropertiesService propertiesService;

	@Autowired
	private StatistiquesRepository statRepository;
	
    @Autowired
    private DemandesFilesRepository demandesFilesRepository;

    @Autowired
    private DemandesComplementsFilesRepository demandesComplementsFilesRepository;

    @Autowired
    private PurgeFilesRepository purgeFilesRepository;

    @Autowired
    private UsagersCache usagerCache;

    @Autowired
    private GouvSchedulerService gouvSchedulerService;

    @Autowired
    private MailTemplateModelProvider mailTemplateModelProvider;
	@Autowired
	private FileService fileService;

	@Autowired
	private DemandesCourriersRepository demandesCourriersRepository;

	@Autowired
	private BrouillonsFilesRepository brouillonsFilesRepository;

	@Override
    public void purgerDemandesDansStatuts(List<String> statuts, int jours) throws JsonProcessingException {
        StringBuilder demandesAPurger = new StringBuilder();
        PropertiesDTO delaiEnvoiEmailProp = propertiesService.getProperty(DELAI_ENVOI_MAIL_PURGE);

        int demandesSuppr = 0;

        LocalDate dateLocaleDebutPurge = LocalDate.now().minusDays(jours - 1L);
        Date dateDebutPurge = Date.from(dateLocaleDebutPurge.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LOGGER.info("Début de la purge des demandes ... Demandes dont dernier statut final est antérieur à {}",
				dateDebutPurge);

        /*** PURGE DES DEMANDES ***/
        Date debutSequentiel = new Date();

		List<Integer> listDem = demandesService.getAllDemandeIdsForPurge(dateDebutPurge, statuts,
                Arrays.asList(GUICHET_VIRTUEL.name(), COURRIER.name(), GUICHET_PHYSIQUE.name()));

		for (Integer demandeId : listDem) {

			demandesService.deleteDemandeInGivenStatus(demandeId, statuts, jours);
			demandesSuppr++;
			LOGGER.info("Demande {} incluse dans un lot. Nombre total traité: {}", demandeId, demandesSuppr);
		}

        /*** MAIL AVANT PURGE ***/
        dateLocaleDebutPurge = LocalDate.now().minusDays(jours - Long.parseLong(delaiEnvoiEmailProp.getValue()));
        dateDebutPurge = Date.from(dateLocaleDebutPurge.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date dateFinPurge;
        dateFinPurge = Date.from(dateLocaleDebutPurge.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        LOGGER.info("Début des envois mails utilisateur ... Demandes dont dernier statut final est >= à {} et < à {}",
				dateDebutPurge, dateFinPurge);
        List<DemandeDTO> listDto = demandesService.getAllDemandeForRelanceAvantPurge(dateDebutPurge,
                dateFinPurge, statuts);
        for (DemandeDTO demandeDTO : listDto) {

			envoisMailUsagerPurge(demandeDTO, delaiEnvoiEmailProp.getValue());

				// Ajout à la liste des demandes à envoyer
            demandesAPurger.append("- ").append(demandeDTO.getIdentifiant()).append(" - ")
                    .append(demandeDTO.getDernierStatut().getName()).append("<br/>");
        }

		// Envois mail agent pour suppression
		if (StringUtils.isNotEmpty(demandesAPurger.toString())) {
			LOGGER.info("Envois du mail au service...");
			envoisMailAgentPurge(demandesAPurger.toString(), delaiEnvoiEmailProp.getValue());
		} else {
			LOGGER.info("Pas d'envois du mail au service car aucune demande purgée...");
		}

        /*** PURGE DES FICHIERS ***/
        Triple<Integer, Integer, Integer> result = executerPurgeFichiers();

        Date finFichier = new Date();

		LOGGER.info("Fin purge des demandes, {} demande(s) supprimée(s)...", demandesSuppr);
        LOGGER.info("Fin purge des demandes, {} fichier(s) supprimé(s)...", result.getLeft());
        LOGGER.info("Fin purge des demandes, {} fichier(s) exclus car référencés...", result.getMiddle());
        LOGGER.info("Fin purge des demandes, {} appels vers file effectué(s)...", result.getRight());
        LOGGER.info("Fin purge des demandes et fichiers en {} secondes",
                (finFichier.getTime() - debutSequentiel.getTime()) / 1000);
        LOGGER.info("fin");
    }

    private Triple<Integer, Integer, Integer> executerPurgeFichiers() {

		Integer compteGlobalFichiers = 0;
		Integer compteGlobalAppelsFile = 0;
		Integer compteGlobalFichiersExclus = 0;
		Iterator<PurgeFilesBO> all = purgeFilesRepository.findAll().iterator();
		List<String> lotCourant = new ArrayList<>();
		int compte = 0;
		LOGGER.info("Début de la purge des fichiers de FILE");
		while (all.hasNext()) {

			PurgeFilesBO cf = all.next();

			if (demandesFilesRepository.findHowManyTimeIsFileReferenced(cf.getUrl()) == 0
					&& demandesCourriersRepository.findHowManyTimeIsFileReferenced(cf.getUrl()) == 0
					&& demandesComplementsFilesRepository.findHowManyTimeIsFileReferenced(cf.getUrl()) == 0
					&& brouillonsFilesRepository.findHowManyTimeIsFileReferenced(cf.getUrl()) == 0) {
				LOGGER.info("Le fichier {} sera effacé de file.", cf.getUrl());

				String url = cf.getUrl();
				if (url != null && url.startsWith("/")) {
					url = url.substring(1);
				}

				lotCourant.add(url);
				compteGlobalFichiers++;
				compte++;
			} else {
				LOGGER.info("Exclusion du fichier {} car référencé ailleurs. Ce fichier ne sera pas supprimé.",
						cf.getUrl());
				compteGlobalFichiersExclus++;
			}

			purgeFilesRepository.delete(cf);

			if (compte == PURGE_DEMANDES_PAR_LOT_TAILLE_FILE || !all.hasNext()) {
				fileService.deleteFiles("ROOT", lotCourant);
				String joined = StringUtils.join(lotCourant, ",");
				LOGGER.info("Appel lot Vers file. Fichiers demandés:{}", joined);
				lotCourant.clear();
				compte = 0;
				compteGlobalAppelsFile++;
			}

		}

		return Triple.of(compteGlobalFichiers, compteGlobalFichiersExclus, compteGlobalAppelsFile);
	}

	private void envoisMailUsagerPurge(DemandeDTO demandeDTO, String delai) {
		final String subjectTemplateCode = "MAIL_PURGE_DEMANDES_POUR_USAGER_OBJET";
		final String bodyTemplateCode = "MAIL_PURGE_DEMANDES_POUR_USAGER_CORPS";

		EmailInfoDTO emailInfoDTO = creationMailPurge(bodyTemplateCode, subjectTemplateCode, demandeDTO.getLangue());

		GichuniUsagerDTO usager = usagerCache.get(demandeDTO.getUsagerId(), true);
		if (usager != null) {

			String prenom = StringUtils.EMPTY;
			String nom = StringUtils.EMPTY;

			if (StringUtils.isNotBlank(usager.getPrenom())) {
				prenom = usager.getPrenom();
			}

			if (StringUtils.isNotBlank(usager.getNom())) {
				nom = usager.getNom();
			}

			emailInfoDTO.addTo(usager.getEmail(), prenom + " " + nom);
			Map<String, Object> model = mailTemplateModelProvider.getGenericModelDemande(demandeDTO);
			model.put("delai", delai);
			model.put("urlFront", gouvPropertiesResolver.getFrontUrl());
			PropertiesDTO adresseService = propertiesService.getProperty("ADRESSE_SERVICE");
	        if(adresseService != null) {
	        	model.put("adresseService", adresseService.getValue());
	        }

			try {
				mailService.sendMail(emailInfoDTO, model);
			} catch (Exception e) {
				LOGGER.error("Erreur lors de l'envoi de l'email de purge pour les agents", e);
			}
		} else {
			LOGGER.info(
					"L'usager {} n'a pas été retrouvé dans le cache, possiblement inexistant dans MonGuichet suite suppression",
					demandeDTO.getUsagerId());
		}
	}

    @Override
	public void envoisMailAgentPurge(String demandesAPurger, String delai) {
		final String subjectTemplateCode = "MAIL_PURGE_DEMANDES_POUR_AGENT_OBJET";
		final String bodyTemplateCode = "MAIL_PURGE_DEMANDES_POUR_AGENT_CORPS";

		EmailInfoDTO emailInfoDTO = creationMailPurge(bodyTemplateCode, subjectTemplateCode, "fr");
		emailInfoDTO.addTo(afBackUtils.getDemarcheInfos().getEmailService(), StringUtils.EMPTY);
		Map<String,Object> model = mailTemplateModelProvider.getGenericModel();
		model.put("demandes", demandesAPurger);
		model.put("delai", delai);

		try {
			mailService.sendMail(emailInfoDTO, model, MailAudienceEnum.AGENT);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'envoi de l'email de purge pour les agents", e);
		}
	}

	@Override
	public Date getDateDerniereExecution() {
		Date date = null;
		try {
			String triggerName = gouvPropertiesResolver.isPaiementEnabled() ? PAIEMENTS_TRIGGER_NAME : DEMANDES_TRIGGER_NAME;
			LOGGER.info("Récupération de la dernière date d'éxecution du job {}.", triggerName);
			Trigger trigger = gouvSchedulerService.getTrigger(triggerName);
			if (trigger != null) {
				date = trigger.getPreviousFireTime();
			}
		} catch (SchedulerException e) {
			LOGGER.error("Aucun trigger pour le job de purge n'a été trouvé");
		}
		return date;
	}

	protected EmailInfoDTO creationMailPurge(String bodyTemplateCode, String subjectTemplateCode, String langue) {

		EmailInfoDTO emailInfo = new EmailInfoDTO();
		emailInfo.setBodyTemplateCode(bodyTemplateCode);
		emailInfo.setSubjectTemplateCode(subjectTemplateCode);
		emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos()
				.getEmailFromNom());
		emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos()
				.getEmailReplytoNom());
		emailInfo.setLangue(langue);

		return emailInfo;
	}

    public List<Object> getDemandesPurgees() {
		LOGGER.info("Récupération des demandes purgées à moins {} mois", OFFSET_MOIS_DATE_PURGE);
		Date dateDebutOffset = Date.from(LocalDateTime.now().minusMonths(OFFSET_MOIS_DATE_PURGE).atZone(ZoneId.systemDefault()).toInstant());
        return statRepository.findAllBetweenDates(demarchesDataProvider.getStatutsAPurger(), dateDebutOffset, new Date());
	}
}
