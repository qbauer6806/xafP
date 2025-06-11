package mc.gouv.xaf.back.service.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.relance.settings.RelanceStatutDemandeConf;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RelancesUtils {

    public static final String NB_JOURS_AVANT_EXPIRATION_KEY = "NB_JOURS_AVANT_EXPIRATION_PAIEMENT";

    private static final String DEMANDE_DEJA_RELANCEE_KEY = "DEMANDE_DEJA_RELANCEE";

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private AfBackUtils afBackUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(RelancesUtils.class);

    public void setRelanceDate(DemandeDTO demande) {
        try {
            demandesDataService.saveOrUpdateDemandeData(demande.getPkDemandes(), DEMANDE_DEJA_RELANCEE_KEY,
                    ZonedDateTime.now().toString());
        } catch (Exception e) {
            LOGGER.error("Erreur lors de demandesDataService.saveOrUpdateDemandeData()", e);
        }
    }

    public boolean isEligiblePourUnMailDeRelance(DemandeDTO demande, Integer nbJoursAvantPremiereRelance,
            Integer nbJoursEntreDeuxRelances, Integer nbMaxRelances) {
        // Date de passage dans le statut "à relancer"
        Instant dateDernierStatut = demande.getDernierStatut().getDate().toInstant();

        // Si on n'a pas de délai pour la première relance, on ne peut rien faire
        if (nbJoursAvantPremiereRelance == null) {
            return false;
        }

        // Date théorique de la première relance
        Instant datePremiereRelance = dateDernierStatut.plus(nbJoursAvantPremiereRelance, ChronoUnit.DAYS);

        // Trop tôt pour relancer
        if (Instant.now().isBefore(datePremiereRelance)) {
            return false;
        }

        DemandeDataDTO demandeData = demandesDataService.getDemandeData(demande.getPkDemandes(),
                DEMANDE_DEJA_RELANCEE_KEY);

        // Si des relances ont déjà eu lieu
        if (demandeData != null) {
            // Si l'intervalle n'est pas défini, pas de relance possible
            if (nbJoursEntreDeuxRelances == null) {
                return false;
            }

            // Vérifie qu'on n'a pas dépassé le nombre max de relances, si défini
            if (nbMaxRelances != null) {
                long joursDepuisPremiereRelance = Duration.between(datePremiereRelance, Instant.now()).toDays();
                long nbRelancesEffectuees = (joursDepuisPremiereRelance / nbJoursEntreDeuxRelances) + 1;

                if (nbRelancesEffectuees >= nbMaxRelances) {
                    return false;
                }
            }

            // Vérifie si on est à la date pour une nouvelle relance
            ZonedDateTime dateDerniereRelance = ZonedDateTime.parse(demandeData.getValue());
            ZonedDateTime dateNouvelleRelance = dateDerniereRelance.plusDays(nbJoursEntreDeuxRelances);

            return dateNouvelleRelance.toInstant().isBefore(Instant.now());
        }

        // Jamais relancé, mais le délai avant la première relance est écoulé
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
        return getExpirationTime(demande, NB_JOURS_AVANT_EXPIRATION_KEY);
    }

    public String getExpirationTime(DemandeDTO demande, String key) {
        String propValue = Optional.ofNullable(propertiesService.getProperty(key)).map(PropertiesDTO::getValue)
                .orElse("0");

        int nbJoursAvantExpiration;
        try {
            nbJoursAvantExpiration = Integer.parseInt(propValue);
        } catch (NumberFormatException e) {
            nbJoursAvantExpiration = 0;
        }

        LocalDate dateStatut = demande.getDernierStatut().getDate().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dateExpiration = dateStatut.plusDays(nbJoursAvantExpiration);
        long days = ChronoUnit.DAYS.between(LocalDate.now(), dateExpiration);

        return String.valueOf(days);
    }

    public Map<DemandeDTO, String> getDemandesANotifier(List<RelanceStatutDemandeConf> relanceDemandeSettings) {
        Map<DemandeDTO, String> result = new HashMap<>();
        for (RelanceStatutDemandeConf relanceDemandeSetting : relanceDemandeSettings) {
            String currentStatut = relanceDemandeSetting.getStatutARelancer();
            int nbJoursAvantPremiereRelance = Integer.parseInt(
                    propertiesService.getProperty(relanceDemandeSetting.getCleDelaiAvantPremiereRelance()).getValue());
            String cleDelaiEntreDeuxRelances = relanceDemandeSetting.getCleDelaiEntreDeuxRelances();
            Integer nbJoursEntreDeuxRelances = null;
            if (cleDelaiEntreDeuxRelances != null) {
                nbJoursEntreDeuxRelances = Integer.parseInt(
                        propertiesService.getProperty(cleDelaiEntreDeuxRelances).getValue());
            }
            // On va chercher toutes les demandes dans le status à expirer
            List<DemandeDTO> demandeDTOList = demandesService.getAllDemandesFilteredByStatut(currentStatut);
            if (null != demandeDTOList && !demandeDTOList.isEmpty()) {
                for (DemandeDTO demandeDTO : demandeDTOList) {
                    if (isEligiblePourUnMailDeRelance(demandeDTO, nbJoursAvantPremiereRelance, nbJoursEntreDeuxRelances,
                            relanceDemandeSetting.getNbMaxRelances())) {
                        // On associe ces demandes au code mail à envoyer si elle est sont éligibles à une relance
                        result.put(demandeDTO, relanceDemandeSetting.getClefMailPrefix());
                    }
                }
            }
        }

        return result;

    }

}
