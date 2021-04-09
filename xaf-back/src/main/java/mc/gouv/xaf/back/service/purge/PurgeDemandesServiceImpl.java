package mc.gouv.xaf.back.service.purge;

import com.fasterxml.jackson.core.JsonProcessingException;
import mc.gouv.xaf.back.data.dao.StatistiquesRepository;
import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PurgeDemandeDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class PurgeDemandesServiceImpl implements PurgeDemandesService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PurgeDemandesServiceImpl.class);

	private final Integer OFFSET_MOIS_DATE_PURGE = 1;

	@Autowired
	private DemandesService demandesService;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private DemarchesDataProvider demarchesDataProvider;

	@Autowired
	private MailService mailService;

	@Autowired
	private AfBackUtils afBackUtils;

	@Autowired
	private StatistiquesRepository statRepository;

	public void purgerDemandesDansStatuts(List<String> statuts, int jours) throws JsonProcessingException {
		String demarcheId = gouvPropertiesResolver.getDemarcheId();
		String demandesAPurger = "";

		LOGGER.info("Début de la purge des demandes ...");

		for (DemandeDTO demandeDTO : demandesService.getAllDemandes(demarcheId)) {
			long diffInMillies = Math.abs(new Date().getTime() - demandeDTO.getDernierStatut().getDate().getTime());
			long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

			if (statuts.contains(demandeDTO.getDernierStatut().getLibelle()) && diff >= jours) {
				// Suppression de la demande
				demandesService.deleteDemande(demarcheId, demandeDTO.getPkDemandes());

			} else if(statuts.contains(demandeDTO.getDernierStatut().getLibelle()) && diff == 1) {
				// L'envois des emails se fait 15 jours avant la supression effective de la demande
				// Envois des emails aux usagers
				envoisMailUsagerPurge(demandeDTO.getIdentifiant(), demandeDTO);

				// Ajout à la liste des demandes à envoyer
				demandesAPurger += "- " + demandeDTO.getIdentifiant() + " - " + demandeDTO.getDernierStatut().getLibelle() + "<br/>";
			}
        }

		LOGGER.info("Envois du mail au service");
		// Envois mail agent pour suppression
		if (StringUtils.isNotEmpty(demandesAPurger)) {
			envoisMailAgentPurge(demandesAPurger);
		}

		LOGGER.info("Fin purge des demandes ...");
	}

    private void envoisMailUsagerPurge(String identifiant, DemandeDTO demandeDTO) {
		final String subjectTemplateCode = "MAIL_PURGE_DEMANDES_POUR_USAGER_OBJET";
		final String bodyTemplateCode = "MAIL_PURGE_DEMANDES_POUR_USAGER_CORPS";

		EmailInfoDTO emailInfoDTO = creationMailPurge(bodyTemplateCode, subjectTemplateCode, demandeDTO.getLangue());
		emailInfoDTO.addTo(demandeDTO.getUsagerEmail(), demandeDTO.getUsagerPrenom() + " " + demandeDTO.getUsagerNom());
		Map<String,Object> model = new HashMap<>();
        model.put("identifiant", identifiant);

        try {
			mailService.sendMail(emailInfoDTO, model);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'envoi de l'email de purge pour les agents", e);
		}
	}

	private void envoisMailAgentPurge(String demandesAPurger) {
		final String subjectTemplateCode = "MAIL_PURGE_DEMANDES_POUR_AGENT_OBJET";
		final String bodyTemplateCode = "MAIL_PURGE_DEMANDES_POUR_AGENT_CORPS";

		EmailInfoDTO emailInfoDTO = creationMailPurge(bodyTemplateCode, subjectTemplateCode, "fr");
		emailInfoDTO.addTo(afBackUtils.getDemarcheInfos().getEmailService(), afBackUtils.getDemarcheInfos()
				.getEmailServiceNom());
		Map<String,Object> model = new HashMap<>();
		model.put("demandes", demandesAPurger);

		try {
			mailService.sendMail(emailInfoDTO, model);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'envoi de l'email de purge pour les usagers", e);
		}
	}

	private EmailInfoDTO creationMailPurge(String bodyTemplateCode, String subjectTemplateCode, String langue) {

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

		List<PurgeDemandeDTO> demandesPurgees = new ArrayList<>();
		for(StatistiqueBO stat : statsDemandesPurgees) {
			PurgeDemandeDTO purgeDemandeDTO = new PurgeDemandeDTO();
			purgeDemandeDTO.setIdentifiantDemande(stat.getIdentifiantDemande());
			purgeDemandeDTO.setDateSuppression(stat.getDate());

			// Recherche du dernier statut non supprimé pour la stat en question
			StatistiqueBO statDernierStatut = statRepository.findFirstByDemandeIdAndStatutPublicNotOrderByDateDesc(stat.getDemandeId(),
					AfBackUtils.STATUT_PUBLIC_SUPPRIMEE);
			purgeDemandeDTO.setDateStatutFinal(statDernierStatut.getDate());
			String statutFinal = demarchesDataProvider.getStatusMap().get(statDernierStatut.getStatutPublic());
			purgeDemandeDTO.setStatutFinal(statutFinal);
			demandesPurgees.add(purgeDemandeDTO);
		}

		return demandesPurgees;
	}
}
