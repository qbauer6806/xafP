package mc.gouv.xaf.back.service.utils;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.expiration.settings.ExpirationStatutDemandeConf;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpirationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpirationUtils.class);
    @Autowired
    private DemandesService demandesService;
    @Autowired
    private PropertiesService propertiesService;
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    public Map<DemandeDTO, String> getDemandesAExpirer(List<ExpirationStatutDemandeConf> expirationDemandeSettings) {
        Map<DemandeDTO, String> result = new HashMap<>();
        for (ExpirationStatutDemandeConf expirationDemandeSetting : expirationDemandeSettings) {
            String currentStatut = expirationDemandeSetting.getStatutAExpirer();
            int nombreJourAvantExpiration = Integer.parseInt(propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), expirationDemandeSetting.getCleDelaiExpiration()).getValue());
            // On va chercher toutes les demandes dans le status à expirer
            List<DemandeDTO> demandes = demandesService.getAllDemandesFilteredByStatut(currentStatut);
            if (null != demandes && !demandes.isEmpty()) {
                for (DemandeDTO demandeDTO : demandes) {
                    // On récupère la date a laquelle la demande est passée dans son statut à expirer
                    Date datePassageStatutAExpirer = demandeDTO.getDernierStatut().getDate();
                    ZonedDateTime now = ZonedDateTime.now();
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
