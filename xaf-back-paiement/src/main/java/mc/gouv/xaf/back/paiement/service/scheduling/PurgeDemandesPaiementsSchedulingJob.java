package mc.gouv.xaf.back.paiement.service.scheduling;

import mc.gouv.xaf.back.paiement.service.purge.PurgePaiementDataService;
import mc.gouv.xaf.back.service.purge.PurgeDemandesService;
import mc.gouv.xaf.back.service.scheduling.PurgeDemandesSchedulingJob;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Job permettant d'appeler le service de purge des demandes.
 * <br>
 * Il est nécessaire de péciser la liste des statuts des demandes à purger.
 * <br>
 * Modification spécifique pour PERMC, on ajoute une purge des données de paiements avant la purge des demandes.
 *
 * @author mboutelier.ext
 */
public class PurgeDemandesPaiementsSchedulingJob extends PurgeDemandesSchedulingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeDemandesPaiementsSchedulingJob.class);
    public static final String JOB_NAME = "PurgeDemandesPaiementsSchedulingJob";

    @Autowired
    private PurgeDemandesService purgeDemandesService;

    @Autowired
    private PurgePaiementDataService purgePaiementDataService;

    @Override
    @Transactional
    public void execute(JobExecutionContext jobExecutionContext) {
        if (getActivationPurge()) {
            try {
                List<String> statuts = getStatuts(jobExecutionContext);
                Integer delaiPurge = getDelaiPurge();
                LOGGER.info("PURGE: Suppression des commandes dont les demandes sont dans les états finaux à moins de {} jours", delaiPurge);
                purgePaiementDataService.purgeData(statuts, delaiPurge);
                LOGGER.info("PURGE: Suppression des demandes dans les états finaux à moins de {} jours", delaiPurge);
                purgeDemandesService.purgerDemandesDansStatuts(statuts, delaiPurge);
            } catch (Exception e) {
                LOGGER.error("Erreur lors de la purge des demandes", e);
            }
        } else {
            LOGGER.info("PURGE: La fonctionnalité de la purge est désactivée, changez la propriété ACTIVATION_PURGE pour activer.");
        }
    }
}
