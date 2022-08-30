package mc.gouv.xaf.back.service.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.relance.RelancesDemandesService;
import mc.gouv.xaf.back.service.relance.settings.RelanceDemandeSettings;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

/**
 * Job permettant d'appeler le service de relance des demandes.
 * <br>
 * Il est nécessaire de péciser la liste des statuts des demandes à purger.
 */
public class RelancesDemandesSchedulingJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelancesDemandesSchedulingJob.class);

    public static final String JOB_NAME = "RelancesDemandesSchedulingJob";
    public static final String TRIGGER_NAME = "RelancesDemandesSchedulingTrigger";

    private static final String ACTIVATION_RAPPEL = "ACTIVATION_RAPPEL";

    @Autowired
    private RelancesDemandesService relanceDemandesService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            PropertiesDTO activationRappel = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), ACTIVATION_RAPPEL);
            boolean active = Boolean.parseBoolean(activationRappel.getValue());
            if (active) {
                List<RelanceDemandeSettings> statutsARelancer = new ArrayList<>();

                // Récupération de la liste des statuts à purger dans le contexte du job detail
                Object statutsJob = jobExecutionContext.getJobDetail().getJobDataMap().get("statutsARelancer");
                if (statutsJob instanceof List) {
                	statutsARelancer = (List<RelanceDemandeSettings>) statutsJob;
                }
                LOGGER.info("RAPPEL COURRIEL: Début du job de relance courriel des demandes pour la démarche {}", gouvPropertiesResolver.getDemarcheId());
                relanceDemandesService.sendRelancesMail(statutsARelancer);
            } else {
                LOGGER.info("RAPPEL COURRIEL: La fonctionnalité de la rappel des courriels est désactivée, changez la propriété ACTIVATION_RAPPEL pour activer.");
            }
        } catch (Exception e) {
            LOGGER.error("Erreur lors du rappel des demandes", e);
        }
    }
}