package mc.gouv.xaf.back.service.utils;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.relance.settings.RelanceStatutDemandeConf;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

@Service
public class RelancesUtils {

	public static final String NB_JOURS_AVANT_EXPIRATION_KEY = "NB_JOURS_AVANT_EXPIRATION";

	private static final String DEMANDE_IC_DEJA_RELANCEE_KEY_PREFIX = "DEMANDE_IC_DEJA_RELANCEE_";

	@Autowired
	private DemandesDataService demandesDataService;

	@Autowired
	private DemandesService demandesService;

	@Autowired
	private PropertiesService propertiesService;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private AfBackUtils afBackUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(RelancesUtils.class);

	public void setRelanceDate(DemandeDTO demande) {
		try {
			demandesDataService.saveOrUpdateDemandeData(gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes(),
					DEMANDE_IC_DEJA_RELANCEE_KEY_PREFIX + demande.getPkDemandes().toString(),
					ZonedDateTime.now().toString());
		} catch (Exception e) {
			LOGGER.error("Erreur lors de demandesDataService.saveOrUpdateDemandeData()", e);
		}
	}

	private Integer getNbJoursAvantExpiration() {
		PropertiesDTO prop = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(),
				NB_JOURS_AVANT_EXPIRATION_KEY);
		if (prop != null) {
			return Integer.parseInt(prop.getValue());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mc.gouv.rescart.service.ExpirationICService#isEligiblePourUnMailDeRelance(mc.
	 * gouv.xaf.shared.dto.DemandeDTO)
	 * 
	 * Méthode définissant si la demande doit faire l'objet d'une relance avant
	 * expiration Si la demande a passé le délai paramétré et que cela fait 1
	 * semaine qu'elle a été relancée on renvoi un mail
	 * 
	 */
	public boolean isEligiblePourUnMailDeRelance(DemandeDTO demande, Integer intervalleEntreDeuxRelance) {
		DemandeDataDTO demandeData = demandesDataService.getDemandeData(gouvPropertiesResolver.getDemarcheId(),
				demande.getPkDemandes(), DEMANDE_IC_DEJA_RELANCEE_KEY_PREFIX + demande.getPkDemandes().toString());
		if (null != demandeData) {
			ZonedDateTime now = ZonedDateTime.now();
			ZonedDateTime dateDerniereRelance = ZonedDateTime.parse(demandeData.getValue());
			// On enlève l'interval entre 2 relances et on compare à la date de la dernière relance
			ZonedDateTime xDaysBetweenLastRelance = now.plusDays(-intervalleEntreDeuxRelance);
			boolean isEligibleMailRelance = dateDerniereRelance.toInstant()
					.isBefore(xDaysBetweenLastRelance.toInstant());
			if (!isEligibleMailRelance) {
				return false;
			}
		}
		// Si on a jamais relancé ou que la demande doit être relancée (ie l'interval entre 2 relances est dépassé) on relance
		return true;
	}

	public EmailInfoDTO creationMailUsager(String bodyTemplateCode, String subjectTemplateCode, String langue) {
		EmailInfoDTO emailInfo = new EmailInfoDTO();
		emailInfo.setBodyTemplateCode(bodyTemplateCode);
		emailInfo.setSubjectTemplateCode(subjectTemplateCode);
		emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
				afBackUtils.getDemarcheInfos().getEmailFromNom());
		emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(),
				afBackUtils.getDemarcheInfos().getEmailReplytoNom());
		emailInfo.setLangue(langue);

		return emailInfo;
	}

	public String getExpirationTime(DemandeDTO demande) {
		Integer nbJoursAvantExpiration = getNbJoursAvantExpiration();
		Date dateStatutEnAttenteIC = demande.getDernierStatut().getDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateStatutEnAttenteIC);
		cal.add(Calendar.DATE, nbJoursAvantExpiration);
		Date dateExpiration = cal.getTime();
		Date currentDate = new Date();
		Long dateExpirationTime = dateExpiration.getTime();
		Long currentDateTime = currentDate.getTime();
		Long diff = dateExpirationTime - currentDateTime;
		long days = TimeUnit.MILLISECONDS.toDays(diff);
		return String.valueOf(days);
	}

	public Map<DemandeDTO, String> getDemandesANotifier(List<RelanceStatutDemandeConf> relanceDemandeSettings) {
		Map<DemandeDTO, String> result = new HashMap<>();
		for (RelanceStatutDemandeConf relanceDemandeSetting : relanceDemandeSettings) {
			String currentStatut = relanceDemandeSetting.getStatutARelancer();
			// On va chercher toutes les demandes dans le status à expirer
			if (null != demandesService.getAllDemandesFilteredByStatut(currentStatut)
					&& !demandesService.getAllDemandesFilteredByStatut(currentStatut).isEmpty()) {
				for (DemandeDTO demandeDTO : demandesService.getAllDemandesFilteredByStatut(currentStatut)) {
					// On récupère la date a laquelle la demande est passée dans son statut à relancer
					Date datePassageStatutARelancer = demandeDTO.getDernierStatut().getDate();
					Integer nbJoursAvantRelance = relanceDemandeSetting.getDelaiAvantPremiereRelance();
					ZonedDateTime xDaysAgoForRelance = ZonedDateTime.now().plusDays(-nbJoursAvantRelance);
					boolean olderThanXDaysForRelance = datePassageStatutARelancer.toInstant()
							.isBefore(xDaysAgoForRelance.toInstant());
					if (isEligiblePourUnMailDeRelance(demandeDTO, relanceDemandeSetting.getDelaiEntreDeuxRelances()) && olderThanXDaysForRelance) {
						// On associe ces demandes au code mail à envoyer si elle est sont éligibles à
						// une relance
						result.put(demandeDTO, relanceDemandeSetting.getClefMailPrefix());
					}
				}
			}
		}
		
		return result;

	}

}
