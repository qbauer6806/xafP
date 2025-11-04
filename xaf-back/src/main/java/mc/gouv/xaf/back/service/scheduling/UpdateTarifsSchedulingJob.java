package mc.gouv.xaf.back.service.scheduling;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.tarif.UpdateTarifsService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author XDECOOL.EXT Job permattant de mettre à jour un tarif en fonction de sa clef Le cron de déclenchement est
 *         quant à lui spécifier dans la configuration propre à chaque besoin
 */
@RequiredArgsConstructor
public class UpdateTarifsSchedulingJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTarifsSchedulingJob.class);

    private final UpdateTarifsService updateTarifsService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            String tarifToUpdateKey = "";

            // Récupération de la liste des statuts à purger dans le contexte du job detail
            Object tarifToUpdate = jobExecutionContext.getJobDetail().getJobDataMap().get("tarifToUpdateKey");
            if (tarifToUpdate instanceof String t) {
                tarifToUpdateKey = t;
            }
            LOGGER.info("UPDATE TARIF: Mise à jour du tarif {}", tarifToUpdateKey);
            updateTarifsService.updateTarifs(tarifToUpdateKey);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'update d'un tarif", e);
        }
    }
}
