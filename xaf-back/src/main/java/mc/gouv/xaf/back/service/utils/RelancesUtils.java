package mc.gouv.xaf.back.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.dto.EmailInfoDTO;
import mc.gouv.xaf.back.service.relance.settings.RelanceStatutDemandeConf;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RelancesUtils {

    public static final String NB_JOURS_AVANT_EXPIRATION_KEY = "NB_JOURS_AVANT_EXPIRATION_PAIEMENT";

    public static final String DATES_RELANCES_KEY = "DATES_RELANCES";

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
            DemandeDataDTO data = Optional.ofNullable(demande.getData()).stream().flatMap(Arrays::stream)
                    .filter(d -> StringUtils.equals(DATES_RELANCES_KEY, d.getKey())).findFirst().orElse(null);

            List<String> dates;
            if (data == null) {
                dates = List.of(ZonedDateTime.now().toString());
            } else {
                dates = MAPPER.readValue(data.getValue(), new TypeReference<>() {

                });
                dates.add(ZonedDateTime.now().toString());
            }
            demandesDataService.saveOrUpdateDemandeData(demande.getPkDemandes(), DATES_RELANCES_KEY,
                    MAPPER.writeValueAsString(dates));
        } catch (Exception e) {
            LOGGER.error("Erreur lors de demandesDataService.saveOrUpdateDemandeData()", e);
        }
    }

    public boolean isEligiblePourUnMailDeRelance(DemandeDTO demande, Integer nbJoursAvantPremiereRelance,
            Integer nbJoursEntreDeuxRelances, Integer nbMaxRelances) {

        ZonedDateTime dateDernierStatut = demande.getDernierStatut().getDate().toInstant()
                .atZone(ZoneId.systemDefault());

        // Historique des dates de relance déjà faites
        DemandeDataDTO data = Optional.ofNullable(demande.getData()).stream().flatMap(Arrays::stream)
                .filter(d -> StringUtils.equals(DATES_RELANCES_KEY, d.getKey())).findFirst().orElse(null);

        ZonedDateTime maintenant = ZonedDateTime.now();

        if (data == null || data.getValue() == null) {
            // Aucune relance encore faite → on vérifie le délai depuis le statut initial
            long joursDepuisStatut = ChronoUnit.DAYS.between(dateDernierStatut, maintenant);
            return joursDepuisStatut >= nbJoursAvantPremiereRelance;
        } else {
            // Il y a déjà eu des relances
            List<ZonedDateTime> relances;
            try {
                List<String> listFromDb = MAPPER.readValue(data.getValue(), new TypeReference<>() {

                });
                relances = listFromDb.stream().map(ZonedDateTime::parse).toList();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            if (nbMaxRelances > 0 && relances.size() >= nbMaxRelances) {
                return false; // trop de relances déjà faites
            }

            // Date de la dernière relance
            ZonedDateTime derniereRelance = relances.stream().max(Comparator.naturalOrder()).orElse(dateDernierStatut);

            long joursDepuisDerniereRelance = ChronoUnit.DAYS.between(derniereRelance, maintenant);
            return joursDepuisDerniereRelance >= nbJoursEntreDeuxRelances;
        }
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
