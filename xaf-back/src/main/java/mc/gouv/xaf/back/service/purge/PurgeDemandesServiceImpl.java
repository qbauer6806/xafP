package mc.gouv.xaf.back.service.purge;

import com.fasterxml.jackson.core.JsonProcessingException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class PurgeDemandesServiceImpl implements PurgeDemandesService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PurgeDemandesServiceImpl.class);

	@Autowired
	private DemandesService demandesService;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private MailService mailService;

	@Autowired
	private AfBackUtils afBackUtils;

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
}
