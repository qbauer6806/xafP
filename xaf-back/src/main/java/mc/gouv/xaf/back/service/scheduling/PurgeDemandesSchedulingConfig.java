package mc.gouv.xaf.back.service.scheduling;

import jakarta.annotation.PostConstruct;

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

    @PostConstruct
    private void init() throws SchedulerException {
        // Init des jobs pour la purge des demandes
        initPurgeJobs();
    }

    private void initPurgeJobs() throws SchedulerException {
        PropertiesDTO prop = propertiesService.getProperty(PURGE_DEMANDES_SCHEDULING_CRON_EXPRESSION);
        JobDetail jobDetail = schedulerService.buildJobDetail(PurgeDemandesSchedulingJob.class,
                "PurgeDemandesSchedulingJob");

        // Ajout de la liste des statuts dans le JobDataMap
        jobDetail.getJobDataMap().put("statuts", demarchesDataProvider.getStatutsAPurger());

        Trigger trigger = schedulerService.buildJobTrigger(jobDetail, "PurgeDemandesSchedulingTrigger",
                prop.getValue());
        schedulerService.startOrUpdateScheduledJob(jobDetail, trigger);
    }
}
