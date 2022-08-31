package mc.gouv.xaf.back.service.utils;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.expiration.settings.ExpirationStatutDemandeConf;
import mc.gouv.xaf.shared.dto.DemandeDTO;

@Service
public class ExpirationUtils {

	@Autowired
	private DemandesService demandesService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpirationUtils.class);

	public Map<DemandeDTO, String> getDemandesAExpirer(List<ExpirationStatutDemandeConf> expirationDemandeSettings) {
		Map<DemandeDTO, String> result = new HashMap<>();
		for (ExpirationStatutDemandeConf expirationDemandeSetting : expirationDemandeSettings) {
			String currentStatut = expirationDemandeSetting.getStatutAExpirer();
			// On va chercher toutes les demandes dans le status à expirer
			if (null != demandesService.getAllDemandesFilteredByStatut(currentStatut)
					&& !demandesService.getAllDemandesFilteredByStatut(currentStatut).isEmpty()) {
				for (DemandeDTO demandeDTO : demandesService.getAllDemandesFilteredByStatut(currentStatut)) {
					// On récupère la date a laquelle la demande est passée dans son statut à expirer
					Date datePassageStatutAExpirer = demandeDTO.getDernierStatut().getDate();
					ZonedDateTime now = ZonedDateTime.now();
					Integer nombreJourAvantExpiration = expirationDemandeSetting.getDelaiExpiration();
					ZonedDateTime xDaysAgoForExpiration = now.plusDays(-nombreJourAvantExpiration);
					boolean doitEtreExpiree = datePassageStatutAExpirer.toInstant().isBefore(xDaysAgoForExpiration.toInstant());
					if (doitEtreExpiree) {
						LOGGER.info(
								"La demande non répondue {} est plus ancienne que {} jours : ordonner l'expiration de la demande",
								demandeDTO.getIdentifiant(), nombreJourAvantExpiration);
						// On associe ces demandes au code mail à envoyer si elle est éligible à
						// une expiration
						result.put(demandeDTO, expirationDemandeSetting.getClefMailPrefix());
					}
				}
			}
		}
		return result;
	}
	

}
