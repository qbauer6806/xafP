package mc.gouv.xaf.back.service.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.purge.PurgeDemandesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

@DisallowConcurrentExecution
public class PurgeDemandesSchedulingJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeDemandesSchedulingJob.class);

    public static final String JOB_NAME = "PurgeDemandesSchedulingJob";
    public static final String TRIGGER_NAME = "PurgeDemandesSchedulingTrigger";

    private static final String DELAI_PURGE_EN_JOURS = "DELAI_PURGE_EN_JOURS";
    private static final String ACTIVATION_PURGE = "ACTIVATION_PURGE";

    @Autowired
    private PurgeDemandesService purgeDemandesService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            PropertiesDTO activationPurge = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), ACTIVATION_PURGE);
            boolean active = Boolean.parseBoolean(activationPurge.getValue());
            if (active) {
                List<String> statuts = new ArrayList<>();

                // Récupération de la liste des statuts à purger dans le contexte du job detail
                Object statutsJob = jobExecutionContext.getJobDetail().getJobDataMap().get("statuts");
                if (statutsJob instanceof List) {
                    statuts = (ArrayList<String>) statutsJob;
                }
                PropertiesDTO delaiPurgeProperty = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), DELAI_PURGE_EN_JOURS);

                LOGGER.info("PURGE: Supression des demandes dans les états finaux à moins de {}", delaiPurgeProperty.getValue());
                purgeDemandesService.purgerDemandesDansStatuts(statuts, Integer.parseInt(delaiPurgeProperty.getValue()));
            } else {
                LOGGER.info("PURGE: La fonctionnalité de la purge est désactivée, changez la propriété ACTIVATION_PURGE pour activer.");
            }
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la purge des demandes", e);
        }
    }
}