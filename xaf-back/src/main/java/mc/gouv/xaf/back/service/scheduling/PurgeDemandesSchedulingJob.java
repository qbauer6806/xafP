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
import mc.gouv.xaf.back.service.purge.PurgeDemandesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

/**
 * Job permettant d'appeler le service de purge des demandes.
 * <br>
 * Il est nécessaire de péciser la liste des statuts des demandes à purger.
 */
public class PurgeDemandesSchedulingJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeDemandesSchedulingJob.class);

    public static final String JOB_NAME = "PurgeDemandesSchedulingJob";
    public static final String TRIGGER_NAME = "PurgeDemandesSchedulingTrigger";

    private static final String DELAI_PURGE_EN_JOURS = "DELAI_PURGE_EN_JOURS";
    private static final int DELAI_PAR_DEFAUT_PURGE = 1095; // 3 ans de purge par défaut
    private static final String ACTIVATION_PURGE = "ACTIVATION_PURGE";

    @Autowired
    private PurgeDemandesService purgeDemandesService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    /**
     * @return La valeur de la propriété d'activation de la purge
     */
    protected boolean getActivationPurge() {
        PropertiesDTO activationPurge = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), ACTIVATION_PURGE);
        return activationPurge != null && Boolean.parseBoolean(activationPurge.getValue());
    }

    /**
     * @return La liste conteant les clés des statuts à purger
     */
    @SuppressWarnings("unchecked")
    protected List<String> getStatuts(JobExecutionContext jobExecutionContext) {
        List<String> statuts = new ArrayList<>();
        // Récupération de la liste des statuts à purger dans le contexte du job detail
        Object statutsJob = jobExecutionContext.getJobDetail().getJobDataMap().get("statuts");
        if (statutsJob instanceof List) {
            statuts = (ArrayList<String>) statutsJob;
        }
        return statuts;
    }

    protected Integer getDelaiPurge() {
        PropertiesDTO delaiPurgeProperty = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), DELAI_PURGE_EN_JOURS);
        return delaiPurgeProperty != null ? Integer.parseInt(delaiPurgeProperty.getValue()) : DELAI_PAR_DEFAUT_PURGE;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        if (getActivationPurge()) {
            try {
                List<String> statuts = getStatuts(jobExecutionContext);
                Integer delaiPurge = getDelaiPurge();
                LOGGER.info("PURGE: Supression des demandes dans les états finaux à moins de {} jours", delaiPurge);
                purgeDemandesService.purgerDemandesDansStatuts(statuts, delaiPurge);
            } catch (Exception e) {
                LOGGER.error("Erreur lors de la purge des demandes", e);
            }
        } else {
            LOGGER.info("PURGE: La fonctionnalité de la purge est désactivée, changez la propriété ACTIVATION_PURGE pour activer.");
        }
    }
}