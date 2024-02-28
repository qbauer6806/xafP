package mc.gouv.xaf.back.service.purge;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.xaf.back.data.dao.DemandesFilesRepository;
import mc.gouv.xaf.back.data.dao.StatistiquesRepository;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.GouvSchedulerService;
import mc.gouv.xaf.back.service.data.DemandesCourriersService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.scheduling.PurgeDemandesSchedulingJob;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.PurgeDemandeDTO;

@Service
@EnableScheduling
public class PurgeDemandesServiceImpl implements PurgeDemandesService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PurgeDemandesServiceImpl.class);

	private static final Integer OFFSET_MOIS_DATE_PURGE = 1;

	private static final String DELAI_ENVOI_MAIL_PURGE = "DELAI_ENVOI_MAIL_PURGE";

	@Autowired
	private DemandesService demandesService;
	
	@Autowired
	private DemandesCourriersService demandesCourriersService;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private DemarchesDataProvider demarchesDataProvider;

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
    private UsagersCache usagerCache;

    @Autowired
    private GouvSchedulerService gouvSchedulerService;

    @Autowired
    private FileService fileService;

    @Autowired
    private DemandesFilesService demandesFilesService;

	public void purgerDemandesDansStatuts(List<String> statuts, int jours) throws Exception {
		String demarcheId = gouvPropertiesResolver.getDemarcheId();
		StringBuilder demandesAPurger = new StringBuilder();
        int demandesSuppr = 0;
		PropertiesDTO delaiEnvoiEmailProp = propertiesService.getProperty(demarcheId, DELAI_ENVOI_MAIL_PURGE);

		LOGGER.info("Début de la purge des demandes ...");

        // AtomicInteger demandesSuppr = new AtomicInteger(0);

        /* PURGE DES DEMANDES */
        LocalDate dateLocaleDebutPurge = LocalDate.now().minusDays(jours);
        Date dateDebutPurge = Date.from(dateLocaleDebutPurge.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Date debutParallel = new Date();
        // TODO: test parralel
        /*
         * demandesService .getAllDemandeIdsForPurge(demarcheId, dateDebutPurge, statuts,
         * Arrays.asList(DemandeCanalEnum.GUICHET_VIRTUEL.name())) .subList(0, 1000).parallelStream().forEach(pkDemande
         * -> { int count = 0;
         * 
         * try { demandesService.deleteDemandeInGivenStatus(demarcheId, pkDemande, statuts, jours); } catch
         * (JsonProcessingException e) { LOGGER.error(
         * "Erreur lors de l'appel a deleteDemandeInGivenStatus lors de la purge de la demande {} ", pkDemande); } //
         * demandesSuppr++; });
         */
        Date finParallel = new Date();

        // TODO: test sequentiel
        Date debutSequentiel = new Date();
        List<Integer> listDem = demandesService.getAllDemandeIdsForPurge(demarcheId, dateDebutPurge, statuts,
                Arrays.asList(DemandeCanalEnum.GUICHET_VIRTUEL.name())).subList(0, 1000);

        List<Integer> listDemLot = new ArrayList<>();
        for (int idx = 0; idx < listDem.size(); idx++) {

            listDemLot.add(listDem.get(idx));
            if (idx == listDemLot.size() - 1 || idx % 8 == 0) {
                demandesService.deleteDemandeBulkInGivenStatus(demarcheId, listDemLot, statuts, jours);
                listDemLot.clear();
            }
            demandesSuppr++;
        }
        Date finSequentiel = new Date();

        // if (1 == 0) {
        for (Integer pkDemande : demandesService.getAllDemandeIdsForPurge(demarcheId, dateDebutPurge, statuts,
                Arrays.asList(DemandeCanalEnum.COURRIER.name(), DemandeCanalEnum.GUICHET_PHYSIQUE.name()))) {
            demandesCourriersService.deleteCourriers(demarcheId, pkDemande);
            demandesService.deleteDemandeInGivenStatus(demarcheId, pkDemande, statuts, jours);
            demandesSuppr++;
        }

        /* MAIL AVANT PURGE */
        dateLocaleDebutPurge = LocalDate.now().minusDays(jours + Integer.parseInt(delaiEnvoiEmailProp.getValue()));
        dateDebutPurge = Date.from(dateLocaleDebutPurge.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date dateFinPurge;
        dateFinPurge = Date.from(dateLocaleDebutPurge.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        for (DemandeDTO demandeDTO : demandesService.getAllDemandeForRelanceAvantPurge(demarcheId, dateDebutPurge,
                dateFinPurge, statuts)) {
            envoisMailUsagerPurge(demandeDTO.getIdentifiant(), demandeDTO, delaiEnvoiEmailProp.getValue());
            // Ajout à la liste des demandes à envoyer
            demandesAPurger.append("- ").append(demandeDTO.getIdentifiant()).append(" - ")
                    .append(demandeDTO.getDernierStatut().getLibelle()).append("<br/>");
        }

		// Envois mail agent pour suppression
		if (StringUtils.isNotEmpty(demandesAPurger.toString())) {
			LOGGER.info("Envois du mail au service...");
			envoisMailAgentPurge(demandesAPurger.toString(), delaiEnvoiEmailProp.getValue());
		} else {
			LOGGER.info("Aucune demande à purger...");
		}
        // }

        // PURGE DES FICHIER
        Date debutFichier = new Date();
        Iterator<DemandesFilesBO> it = demandesFilesRepository.findAllNonReferencedFiles().iterator();
        while (it.hasNext()) {
            DemandesFilesBO fichierOrphelin = (DemandesFilesBO) it.next();

            Integer refs = demandesFilesRepository.findHowManyTimeIsFileReferenced(fichierOrphelin.getUrl());
            LOGGER.debug("L'url du fichier est utilisée par {}", refs);
            if (refs.intValue() == 0) {
                try {
                    String url = URLEncoder.encode(fichierOrphelin.getUrl(), "UTF-8");
                    fileService.deleteFile("ROOT", url);
                } catch (UnsupportedEncodingException e) {
                    LOGGER.error("Problème lors de l'encoding des urls des fichiers initiaux", e);
                }
            }
            Date finFichier = new Date();
            demandesFilesRepository.delete(fichierOrphelin);
        }

        LOGGER.info("parallel debut:{}", debutParallel);
        LOGGER.info("parellel debut:{}", finParallel);
        LOGGER.info("sequentiel debut:{}", debutSequentiel);
        LOGGER.info("sequentiel debut:{}", finSequentiel);
        LOGGER.info("fichiers debut:{}", debutFichier);
        LOGGER.info("fichiers debut:{}", debutFichier);

		LOGGER.info("Fin purge des demandes, {} demande(s) supprimée(s)...", demandesSuppr);
	}

    private void envoisMailUsagerPurge(String identifiant, DemandeDTO demandeDTO, String delai) {
		final String subjectTemplateCode = "MAIL_PURGE_DEMANDES_POUR_USAGER_OBJET";
		final String bodyTemplateCode = "MAIL_PURGE_DEMANDES_POUR_USAGER_CORPS";

		EmailInfoDTO emailInfoDTO = creationMailPurge(bodyTemplateCode, subjectTemplateCode, demandeDTO.getLangue());
		
        UsagerBean usager = usagerCache.get(demandeDTO.getUsagerId(), true);
        if (usager == null) {
            usager = new UsagerBean();
            usager.setNom(demandeDTO.getUsagerNom());
            usager.setPrenom(demandeDTO.getUsagerPrenom());
            usager.setEmail(demandeDTO.getUsagerEmail());
        }
        
        String prenom = StringUtils.EMPTY;
        String nom = StringUtils.EMPTY;

        if (StringUtils.isNotBlank(usager.getPrenom())) {
            prenom = usager.getPrenom();
        }

        if (StringUtils.isNotBlank(usager.getNom())) {
            nom = usager.getNom();
        }
		
		emailInfoDTO.addTo(usager.getEmail(), prenom + " " + nom);
		Map<String,Object> model = new HashMap<>();
        model.put("identifiant", identifiant);
        model.put("delai", delai);

        try {
			mailService.sendMail(emailInfoDTO, model);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'envoi de l'email de purge pour les agents", e);
		}
	}

    @Override
	public void envoisMailAgentPurge(String demandesAPurger, String delai) {
		final String subjectTemplateCode = "MAIL_PURGE_DEMANDES_POUR_AGENT_OBJET";
		final String bodyTemplateCode = "MAIL_PURGE_DEMANDES_POUR_AGENT_CORPS";

		EmailInfoDTO emailInfoDTO = creationMailPurge(bodyTemplateCode, subjectTemplateCode, "fr");
		emailInfoDTO.addTo(afBackUtils.getDemarcheInfos().getEmailService(), afBackUtils.getDemarcheInfos()
				.getEmailServiceNom());
		Map<String,Object> model = new HashMap<>();
		model.put("demandes", demandesAPurger);
		model.put("delai", delai);

		try {
			mailService.sendMail(emailInfoDTO, model);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'envoi de l'email de purge pour les usagers", e);
		}
	}

	@Override
	public Date getDateDerniereExecution() {
		Date date = null;
		try {
			Trigger trigger = gouvSchedulerService.getTrigger(PurgeDemandesSchedulingJob.TRIGGER_NAME);
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

	public List<PurgeDemandeDTO> getDemandesPurgees() {
		LOGGER.info("Récupération des demandes purgées à moins {} mois", OFFSET_MOIS_DATE_PURGE);
		Date dateDebutOffset = Date.from(LocalDateTime.now().minusMonths(OFFSET_MOIS_DATE_PURGE).atZone(ZoneId.systemDefault()).toInstant());
		List<StatistiqueBO> statsDemandesPurgees = statRepository.findByStatutPublicAndDateBetween(AfBackUtils.STATUT_PUBLIC_SUPPRIMEE,
				dateDebutOffset , new Date());
		statsDemandesPurgees.sort(Comparator.comparing(StatistiqueBO::getDate));
		LOGGER.info("{} ligne(s) de statistiques de demandes purgées...", statsDemandesPurgees.size());

		List<PurgeDemandeDTO> demandesPurgees = new ArrayList<>();
		for(StatistiqueBO stat : statsDemandesPurgees) {
			PurgeDemandeDTO purgeDemandeDTO = new PurgeDemandeDTO();
			purgeDemandeDTO.setIdentifiantDemande(stat.getIdentifiantDemande());
			purgeDemandeDTO.setDateSuppression(stat.getDate());

			// Recherche du dernier statut non supprimé pour la stat en question
			StatistiqueBO statDernierStatut = statRepository.findFirstByDemandeIdAndStatutPublicNotOrderByDateDesc(stat.getDemandeId(),
					AfBackUtils.STATUT_PUBLIC_SUPPRIMEE);
			if (null != statDernierStatut) {
				purgeDemandeDTO.setDateStatutFinal(statDernierStatut.getDate());
				String statutFinal = demarchesDataProvider.getStatusMap().get(statDernierStatut.getStatutPublic());
				purgeDemandeDTO.setStatutFinal(statutFinal);
			}

			demandesPurgees.add(purgeDemandeDTO);
		}

		return demandesPurgees;
	}
}
