package mc.gouv.xaf.back.service.scheduling;

import java.util.ArrayList;
import java.util.List;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.relance.RelancesDemandesService;
import mc.gouv.xaf.back.service.relance.settings.RelanceStatutDemandeConf;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Job permettant d'appeler le service de relance des demandes.
 * <br>
 * Il est nécessaire de péciser la liste des statuts des demandes à purger.
 */
public class RelancesDemandesSchedulingJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelancesDemandesSchedulingJob.class);

    public static final String JOB_NAME = "RelancesDemandesSchedulingJob";
    public static final String TRIGGER_NAME = "RelancesDemandesSchedulingTrigger";

    private static final String XAF_RAPPEL_ACTIVATION = "XAF_RAPPEL_ACTIVATION";

    @Autowired
    private RelancesDemandesService relanceDemandesService;

    @Autowired
    private PropertiesService propertiesService;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            PropertiesDTO activationRappel = propertiesService.getProperty(XAF_RAPPEL_ACTIVATION);
            boolean active = Boolean.parseBoolean(activationRappel.getValue());
            if (active) {
                List<RelanceStatutDemandeConf> confRelances = new ArrayList<>();

                // Récupération de la liste des statuts à purger dans le contexte du job detail
                Object statutsJob = jobExecutionContext.getJobDetail().getJobDataMap().get("statutsARelancer");
                if (statutsJob instanceof List) {
                    confRelances = (List<RelanceStatutDemandeConf>) statutsJob;
                }
                LOGGER.info("RAPPEL COURRIEL: Début du job de relance courriel des demandes");
                relanceDemandesService.sendRelancesMail(confRelances);
            } else {
                LOGGER.info(
                        "RAPPEL COURRIEL: La fonctionnalité de le relance est désactivée, changez la propriété XAF_RAPPEL_ACTIVATION pour activer.");
            }
        } catch (Exception e) {
            LOGGER.error("Erreur lors du rappel des demandes", e);
        }
    }
}
