package mc.gouv.xaf.back.service.scheduling;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Job permettant l'expiration des consentements porte-documents périmés côté TS
 */
@Component
@RequiredArgsConstructor
public class ExpirationDocHolderConsentSchedulingJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpirationDocHolderConsentSchedulingJob.class);

    private final AccessRepository accessRepository;
    private final PropertiesService propertiesService;

    private static final String DOCHOLDER_CONSENT_NODE = "docholderConsent";
    private static final String CONSENTING_NODE = "consenting";
    private static final String DATE_CREATION_NODE = "dateCreation";
    private static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final String XAF_PORTE_DOCUMENT_ACTIF = "XAF_PORTE_DOCUMENT_ACTIF";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOGGER.info("====================== Démarrage du job ExpirationDocHolderConsentSchedulingJob");

        PropertiesDTO docHolderEnabled = propertiesService.getProperty(XAF_PORTE_DOCUMENT_ACTIF);

        if (docHolderEnabled == null) {
            LOGGER.error(
                    "Impossible de lancer le job d'expiration du consentement du porte-documents : la propriété {} n'a pas été trouvée.",
                    XAF_PORTE_DOCUMENT_ACTIF);
            return;
        }

        boolean isDocHolderEnabled = Boolean.parseBoolean(docHolderEnabled.getValue());
        if (isDocHolderEnabled) {
            ObjectMapper mapper = new ObjectMapper();
            List<AccessBO> accesses = accessRepository.findByActive(true);

            // Si 1 an + 1 mois il faut périmer le consentement TS du porte-documents
            for (AccessBO access : accesses) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(JSON_DATE_FORMAT);
                    JsonNode contenu = mapper.readTree(access.getContenu());
                    if (!contenu.findPath(DOCHOLDER_CONSENT_NODE).isEmpty()) {
                        JsonNode dateNode = contenu.findPath(DOCHOLDER_CONSENT_NODE).findPath(DATE_CREATION_NODE);
                        boolean isConsenting = contenu.findPath(DOCHOLDER_CONSENT_NODE).findPath(CONSENTING_NODE)
                                .asBoolean();
                        Date dateConsent = dateFormat.parse(dateNode.asString());
                        Date oneYearPlusOneMonth = Date.from(
                                dateConsent.toInstant().atZone(ZoneId.of("Europe/Monaco")).plusYears(1).plusMonths(1)
                                        .toInstant());
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
                } catch (JacksonException e) {
                    LOGGER.error("Impossible de parser le contenu des données d'accès en JSON", e);
                }
            }
        } else {
            LOGGER.error(
                    "Impossible de lancer le job d'expiration du consentement du porte-documents. Le porte-documents n'est pas activé.");
        }

        LOGGER.info("====================== Fin du job ExpirationDocHolderConsentSchedulingJob");
    }
}
