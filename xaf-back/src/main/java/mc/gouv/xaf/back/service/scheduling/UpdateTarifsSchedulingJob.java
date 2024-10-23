package mc.gouv.xaf.back.service.scheduling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import mc.gouv.xaf.back.service.tarif.UpdateTarifsService;

/**
 * @author XDECOOL.EXT Job permattant de mettre à jour un tarif en fonction de sa clef Le cron de déclenchement est
 *         quant à lui spécifier dans la configuration propre à chaque besoin
 */
public class UpdateTarifsSchedulingJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTarifsSchedulingJob.class);

    @Autowired
    private UpdateTarifsService updateTarifsService;

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
