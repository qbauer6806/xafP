package mc.gouv.xaf.back.service.scheduling;

import jakarta.annotation.PostConstruct;

import mc.gouv.xaf.back.service.purge.PurgeDemandesService;
import mc.gouv.xaf.shared.util.PurgeJobSelector;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.GouvSchedulerService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

@Service
public class PurgeDemandesSchedulingConfig {

    private static final String PURGE_DEMANDES_SCHEDULING_CRON_EXPRESSION = "PURGE_DEMANDES_SCHEDULING_CRON_EXPRESSION";

    @Autowired
    private GouvSchedulerService schedulerService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private PurgeJobSelector jobSelector;

    @PostConstruct
    private void init() throws SchedulerException {
        // Init des jobs pour la purge des demandes
        initPurgeJobs();
    }

    private void initPurgeJobs() throws SchedulerException {
        PropertiesDTO prop = propertiesService.getProperty(PURGE_DEMANDES_SCHEDULING_CRON_EXPRESSION);

        // Sélection de la classe du Job en fonction du contexte
        boolean purgePaiement = demarchesDataProvider.purgerDonneesMonetiques();
        Class<? extends Job> jobClass = jobSelector.getJobClass(purgePaiement);

        // Construction du nom dynamiquement
        String jobName = jobClass.getSimpleName();
        String triggerName = purgePaiement ? PurgeDemandesService.PAIEMENTS_TRIGGER_NAME : "PurgeDemandesSchedulingTrigger";

        // Construction du job et du trigger
        JobDetail jobDetail = schedulerService.buildJobDetail(jobClass, jobName);
        Trigger trigger = schedulerService.buildJobTrigger(jobDetail, triggerName, prop.getValue());

        // Ajout des données
        jobDetail.getJobDataMap().put("statuts", demarchesDataProvider.getStatutsAPurger());

        // Planification
        schedulerService.startOrUpdateScheduledJob(jobDetail, trigger);
    }
}
