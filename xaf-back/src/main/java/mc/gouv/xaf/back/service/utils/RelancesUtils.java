package mc.gouv.xaf.back.service.utils;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

@Service
public class RelancesUtils {

	private static final String NB_JOURS_AVANT_RELANCE_KEY = "NB_JOURS_AVANT_RELANCE";

	public static final String NB_JOURS_AVANT_EXPIRATION_KEY = "NB_JOURS_AVANT_EXPIRATION";

	private static final String NB_JOURS_ENTRE_DEUX_RELANCE_KEY = "NB_JOURS_ENTRE_DEUX_RELANCE";

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

	public Integer getNbJoursAvantRelance() {
		PropertiesDTO prop = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(),
				NB_JOURS_AVANT_RELANCE_KEY);
		if (prop != null) {
			return Integer.parseInt(prop.getValue());
		}
		return null;
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
	public boolean isEligiblePourUnMailDeRelance(DemandeDTO demande) {
		DemandeDataDTO demandeData = demandesDataService.getDemandeData(gouvPropertiesResolver.getDemarcheId(),
				demande.getPkDemandes(), DEMANDE_IC_DEJA_RELANCEE_KEY_PREFIX + demande.getPkDemandes().toString());
		if (null != demandeData) {
			ZonedDateTime now = ZonedDateTime.now();
			ZonedDateTime dateDerniereRelance = ZonedDateTime.parse(demandeData.getValue());
			// On enlève une semaine et on compare à la date de la dernière relance
			Integer intervalleEntreDeuxRelance = getIntervalleEntreDeuxRelance();
			ZonedDateTime xDaysBetweenLastRelance = now.plusDays(-intervalleEntreDeuxRelance);
			boolean isEligibleMailRelance = dateDerniereRelance.toInstant()
					.isBefore(xDaysBetweenLastRelance.toInstant());
			if (!isEligibleMailRelance) {
				return false;
			}
		}
		// Si on a jamais relancé ou que la demande doit être relancée (ie ça fait une
		// semaine qu'elle a pas été relancée) on relance
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

	private Integer getIntervalleEntreDeuxRelance() {
		PropertiesDTO prop = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(),
				NB_JOURS_ENTRE_DEUX_RELANCE_KEY);
		if (prop != null) {
			return Integer.parseInt(prop.getValue());
		}
		return null;
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

	public Map<DemandeDTO, String> getDemandesANotifier(Map<String, String> statutsARelancer) {
		Set<String> statusARelancerKey = statutsARelancer.keySet();
		// On recupère les demandes liées à ce status
		// On crée une map qui associe une demande et son code mail
		Map<DemandeDTO, String> associationFinale = new HashMap<>();
		for (String currentStatut : statusARelancerKey) {
			if (null != demandesService.getAllDemandesFilteredByStatut(currentStatut)
					&& !demandesService.getAllDemandesFilteredByStatut(currentStatut).isEmpty()) {
				String codeMail = statutsARelancer.get(currentStatut);
				for (DemandeDTO demandeDTO : demandesService.getAllDemandesFilteredByStatut(currentStatut)) {
					associationFinale.put(demandeDTO, codeMail);
				}
			}
		}

		// On associe ces demandes au code mail à envoyer si elle est sont éligibles à
		// une relance
		Map<DemandeDTO, String> result = new HashMap<>();
		for (Map.Entry<DemandeDTO, String> entry : associationFinale.entrySet()) {
			Integer nbJoursAvantRelance = getNbJoursAvantRelance();
			Date datePassageStatutARelancer = entry.getKey().getDernierStatut().getDate();
			ZonedDateTime xDaysAgoForRelance = ZonedDateTime.now().plusDays(-nbJoursAvantRelance);
			boolean olderThanXDaysForRelance = datePassageStatutARelancer.toInstant()
					.isBefore(xDaysAgoForRelance.toInstant());
			if (isEligiblePourUnMailDeRelance(entry.getKey()) && olderThanXDaysForRelance) {
				result.put(entry.getKey(), entry.getValue());
				// Envoi du mail
				// Set de la date de relance
				setRelanceDate(entry.getKey());
			}
		}
		return result;

	}

}
