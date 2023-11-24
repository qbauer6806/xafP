package mc.gouv.xaf.back.service.scheduling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Job permettant l'expiration des consentements porte-documents périmés côté TS
 */
public class ExpirationDocHolderConsentSchedulingJob implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpirationDocHolderConsentSchedulingJob.class);
    @Autowired
    private AccessRepository accessRepository;
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    private static final String JOB_NAME = "ExpirationDocHolderConsentSchedulingJob";
    private static final String TRIGGER_NAME = "ExpirationDocHolderConsentSchedulingTrigger";
    private static final String DOCHOLDER_CONSENT_NODE = "docholderConsent";
    private static final String CONSENTING_NODE = "consenting";
    private static final String DATE_CREATION_NODE = "dateCreation";
    private static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOGGER.info("====================== Démarrage du job ExpirationDocHolderConsentSchedulingJob");
        if (gouvPropertiesResolver.isPorteDocEnabled()) {
            ObjectMapper mapper = new ObjectMapper();
            List<AccessBO> accesses = accessRepository.getByDemarcheIdAndActive(gouvPropertiesResolver.getDemarcheId(), true);

            // Si 1 an + 1 mois il faut périmer le consentement TS du porte-documents
            for (AccessBO access : accesses) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(JSON_DATE_FORMAT);
                    JsonNode contenu = mapper.readTree(access.getContenu());
                    if (!contenu.findPath(DOCHOLDER_CONSENT_NODE).isEmpty()) {
                        JsonNode dateNode = contenu.findPath(DOCHOLDER_CONSENT_NODE).findPath(DATE_CREATION_NODE);
                        boolean isConsenting = contenu.findPath(DOCHOLDER_CONSENT_NODE).findPath(CONSENTING_NODE).asBoolean();
                        Date dateConsent = dateFormat.parse(dateNode.asText());
                        Date oneYearPlusOneMonth = Date.from(dateConsent.toInstant().atZone(ZoneId.of("Europe/Monaco")).plusYears(1).plusMonths(1).toInstant());
                        Date today = Date.from(Instant.now().atZone(ZoneId.of("Europe/Monaco")).toInstant());

                        if (oneYearPlusOneMonth.before(today) && isConsenting) {
                            LOGGER.info("Expiration du consentement de l'accès {}", access.getPkAccess());
                            ((ObjectNode) contenu.findPath(DOCHOLDER_CONSENT_NODE)).put(CONSENTING_NODE, false);

                            access.setContenu(contenu.toString());

                            accessRepository.save(access);
                        }
                    }
                } catch (ParseException e) {
                    LOGGER.error("Impossible de parser la date de consentement au porte-documents de l'utilisateur", e);
                } catch (JsonProcessingException e) {
                    LOGGER.error("Impossible de parser le contenu des données d'accès en JSON", e);
                }
            }
        } else {
            LOGGER.error("Impossible de lancer le job d'expiration du consentement du porte-documents. Le porte-documents n'est pas activé.");
        }

        LOGGER.info("====================== Fin du job ExpirationDocHolderConsentSchedulingJob");
    }
}
