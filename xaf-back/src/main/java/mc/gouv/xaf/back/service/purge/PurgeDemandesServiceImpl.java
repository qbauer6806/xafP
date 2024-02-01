package mc.gouv.xaf.back.service.purge;

import com.fasterxml.jackson.core.JsonProcessingException;
import mc.gouv.xaf.back.data.dao.StatistiquesRepository;
import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.GouvSchedulerService;
import mc.gouv.xaf.back.service.data.DemandesCourriersService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.data.TachesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.PurgeDemandeDTO;
import mc.gouv.xaf.shared.enums.MailAudienceEnum;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private UsagersCache usagerCache;

    @Autowired
    private GouvSchedulerService gouvSchedulerService;
    
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private TachesService tachesService;

    public void purgerDemandesDansStatuts(List<String> statuts, int jours) throws JsonProcessingException {
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        StringBuilder demandesAPurger = new StringBuilder();
        int demandesSuppr = 0;
        PropertiesDTO delaiEnvoiEmailProp = propertiesService.getProperty(demarcheId, DELAI_ENVOI_MAIL_PURGE);

		LOGGER.info("Début de la purge des demandes ...");

		for (DemandeDTO demandeDTO : demandesService.getAllDemandes(demarcheId)) {
			long diffInMillies = Math.abs(new Date().getTime() - demandeDTO.getDernierStatut().getDate().getTime());
			long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

			if (statuts.contains(demandeDTO.getDernierStatut().getLibelle()) && diff >= jours) {
				// Si la demande est une demande courrier ou gichet on supprime d'abord les courriers associés à cette demande
				if(!demandeDTO.getCanal().equals(DemandeCanalEnum.GUICHET_VIRTUEL)) {
					// Suppression des courriers de la demande
					demandesCourriersService.deleteCourriers(demarcheId, demandeDTO.getPkDemandes());
				}
				// Ensuite on supprime la demande elle même
				demandesService.deleteDemandeInGivenStatus(demarcheId, demandeDTO.getPkDemandes(), statuts, jours);
				demandesSuppr++;

            } else if (statuts.contains(demandeDTO.getDernierStatut().getLibelle()) && diff == jours - Long.parseLong(delaiEnvoiEmailProp.getValue())) {
                // L'envois des emails se fait 15 jours avant la supression effective de la demande
                // Envois des emails aux usagers
                envoisMailUsagerPurge(demandeDTO.getIdentifiant(), demandeDTO, delaiEnvoiEmailProp.getValue());

				// Ajout à la liste des demandes à envoyer
				demandesAPurger.append("- ").append(demandeDTO.getIdentifiant()).append(" - ").append(demandeDTO.getDernierStatut().getLibelle()).append("<br/>");
			}
        }

		// Envois mail agent pour suppression
		if (StringUtils.isNotEmpty(demandesAPurger.toString())) {
			LOGGER.info("Envois du mail au service...");
			envoisMailAgentPurge(demandesAPurger.toString(), delaiEnvoiEmailProp.getValue());
		} else {
			LOGGER.info("Aucune demande à purger...");
		}

		LOGGER.info("Fin purge des demandes, {} demande(s) supprimée(s)...", demandesSuppr);
	}

    private void envoisMailUsagerPurge(String identifiant, DemandeDTO demandeDTO, String delai) {
		final String subjectTemplateCode = "MAIL_PURGE_DEMANDES_POUR_USAGER_OBJET";
		final String bodyTemplateCode = "MAIL_PURGE_DEMANDES_POUR_USAGER_CORPS";

		EmailInfoDTO emailInfoDTO = creationMailPurge(bodyTemplateCode, subjectTemplateCode, demandeDTO.getLangue());
		
        GichuniUsagerDTO usager = usagerCache.get(demandeDTO.getUsagerId(), true);
        if (usager == null) {
            usager = new GichuniUsagerDTO();
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
        model.put("pkDemande", demandeDTO.getPkDemandes());
        model.put("delai", delai);
        String titre = messageSource.getMessage("civilite." + usager.getTitre(), null, new Locale(demandeDTO.getLangue()));
        model.put("titre", titre);
        model.put("urlFront", gouvPropertiesResolver.getFrontUrl());
        PropertiesDTO adresseService = propertiesService.getProperty(demandeDTO.getDemarcheId(), "ADRESSE_SERVICE");
        if(adresseService != null) {
        	model.put("adresseService", adresseService.getValue());
        }

        try {
            mailService.sendMail(emailInfoDTO, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email de purge pour les usagers", e);
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
