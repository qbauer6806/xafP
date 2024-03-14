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
import org.apache.commons.lang3.tuple.Triple;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.xaf.back.data.dao.BrouillonsFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesComplementsFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesCourriersRepository;
import mc.gouv.xaf.back.data.dao.DemandesFilesRepository;
import mc.gouv.xaf.back.data.dao.PurgeFilesRepository;
import mc.gouv.xaf.back.data.dao.StatistiquesRepository;
import mc.gouv.xaf.back.data.entity.PurgeFilesBO;
import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.GouvSchedulerService;
import mc.gouv.xaf.back.service.data.DemandesCourriersService;
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

    private static final Integer PURGE_DEMANDES_PAR_LOT_TAILLE_FILE = 100;

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
    private DemandesComplementsFilesRepository demandesComplementsFilesRepository;


    @Autowired
    private PurgeFilesRepository purgeFilesRepository;

    @Autowired
    private UsagersCache usagerCache;

    @Autowired
    private GouvSchedulerService gouvSchedulerService;

    @Autowired
    private FileService fileService;

    @Autowired
    private DemandesCourriersRepository demandesCourriersRepository;

    @Autowired
    private BrouillonsFilesRepository brouillonsFilesRepository;

    public void purgerDemandesDansStatuts(List<String> statuts, int jours) throws Exception {

        String demarcheId = gouvPropertiesResolver.getDemarcheId();
		StringBuilder demandesAPurger = new StringBuilder();
        PropertiesDTO delaiEnvoiEmailProp = propertiesService.getProperty(demarcheId, DELAI_ENVOI_MAIL_PURGE);


        int demandesSuppr = 0;

        LocalDate dateLocaleDebutPurge = LocalDate.now().minusDays(jours - 1);
        Date dateDebutPurge = Date.from(dateLocaleDebutPurge.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LOGGER.info("Début de la purge des demandes ... Demandes dont dernier statut final est antérieur à {}", dateDebutPurge);
        
        /*** PURGE DES DEMANDES CANAL WEB ***/
        Date debutSequentiel = new Date();
        List<Integer> listDem = demandesService.getAllDemandeIdsForPurge(demarcheId, dateDebutPurge, statuts,
                Arrays.asList(DemandeCanalEnum.GUICHET_VIRTUEL.name()));

        for (int idx = 0; idx < listDem.size(); idx++) {

            demandesService.deleteDemandeInGivenStatus(demarcheId, listDem.get(idx), statuts, jours);
            demandesSuppr++;
            LOGGER.info("Demande {} incluse dans un lot. Nombre total traité: {}", listDem.get(idx), demandesSuppr);
        }

        /*** PURGE DES DEMANDES CANAL COURRIER OU GUICHET ***/
        listDem = demandesService.getAllDemandeIdsForPurge(demarcheId, dateDebutPurge, statuts,
                Arrays.asList(DemandeCanalEnum.COURRIER.name(), DemandeCanalEnum.GUICHET_PHYSIQUE.name()));


        for (int idx = 0; idx < listDem.size(); idx++) {

            demandesService.deleteDemandeInGivenStatus(demarcheId, listDem.get(idx), statuts, jours);
            demandesCourriersService.deleteCourriers(demarcheId, listDem.get(idx));
            demandesSuppr++;
            LOGGER.info("Demande {} incluse dans un lot. Nombre total traité: {}", listDem.get(idx), demandesSuppr);
        }

        /*** MAIL AVANT PURGE ***/
        dateLocaleDebutPurge = LocalDate.now().minusDays(jours - Integer.parseInt(delaiEnvoiEmailProp.getValue()));
        dateDebutPurge = Date.from(dateLocaleDebutPurge.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date dateFinPurge;
        dateFinPurge = Date.from(dateLocaleDebutPurge.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        LOGGER.info("Début des envois mails utilisateur ... Demandes dont dernier statut final est >= à {} et < à {}", dateDebutPurge, dateFinPurge);
        List<DemandeDTO> listDto = demandesService.getAllDemandeForRelanceAvantPurge(demarcheId, dateDebutPurge,
                dateFinPurge, statuts);
        for (DemandeDTO demandeDTO : listDto) {

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
                LOGGER.info("Appel lot Vers file. Fichiers demandés:{}", StringUtils.join(lotCourant, ","));
                lotCourant.clear();
                compte = 0;
                compteGlobalAppelsFile++;
            }

        }

        return Triple.of(compteGlobalFichiers, compteGlobalFichiersExclus, compteGlobalAppelsFile);
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
