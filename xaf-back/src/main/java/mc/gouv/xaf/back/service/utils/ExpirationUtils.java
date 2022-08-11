package mc.gouv.xaf.back.service.utils;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

@Service
public class ExpirationUtils {

	private static String NB_JOURS_AVANT_EXPIRATION_KEY = "NB_JOURS_AVANT_EXPIRATION";

	@Autowired
	private DemandesService demandesService;

	@Autowired
	private PropertiesService propertiesService;
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpirationUtils.class);

	public Map<DemandeDTO, String> getDemandesAExpirer(Map<String, String> statutsAExpirer) {
		Set<String> statusARelancerKey = statutsAExpirer.keySet();
		// On recupère les demandes liées à ce status
		// On crée une map qui associe une demande et son code mail
		Map<DemandeDTO, String> associationFinale = new HashMap<>();
		for (String currentStatut : statusARelancerKey) {
			if (null != demandesService.getAllDemandesFilteredByStatut(currentStatut)
					&& !demandesService.getAllDemandesFilteredByStatut(currentStatut).isEmpty()) {
				String codeMail = statutsAExpirer.get(currentStatut);
				for (DemandeDTO demandeDTO : demandesService.getAllDemandesFilteredByStatut(currentStatut)) {
					associationFinale.put(demandeDTO, codeMail);
				}
			}
		}

		// On associe ces demandes au code mail à envoyer si elle est sont éligibles à
		// une expiration
		Map<DemandeDTO, String> result = new HashMap<>();
		for (Map.Entry<DemandeDTO, String> entry : associationFinale.entrySet()) {
			// On récupère la date a laquelle la demande est passée en IC
			Date datePassageStatutAExpirer = entry.getKey().getDernierStatut().getDate();
			ZonedDateTime now = ZonedDateTime.now();
			Integer nombreJourAvantExpiration = getNbJoursAvantExpiration();
			ZonedDateTime xDaysAgoForExpiration = now.plusDays(-nombreJourAvantExpiration);
			boolean doitEtreExpiree = datePassageStatutAExpirer.toInstant().isBefore(xDaysAgoForExpiration.toInstant());
			if (doitEtreExpiree) {
				LOGGER.info(
						"La demande non répondue {} est plus ancienne que {} jours : ordonner l'expiration de la demande",
						entry.getKey().getIdentifiant(), nombreJourAvantExpiration);
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
	
	private Integer getNbJoursAvantExpiration() {
		PropertiesDTO prop = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(),
				NB_JOURS_AVANT_EXPIRATION_KEY);
		if (prop != null) {
			return Integer.parseInt(prop.getValue());
		}
		return null;
	}
	
	

}
