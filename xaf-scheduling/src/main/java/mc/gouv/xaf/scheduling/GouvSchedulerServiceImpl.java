package mc.gouv.xaf.scheduling;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GouvSchedulerServiceImpl implements GouvSchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvSchedulerServiceImpl.class);

    @Autowired
    private Scheduler scheduler;

    /**
     * Création d'un job quartz
     * @param clazz Class du job à executer
     * @param name Nom du job
     */
    public JobDetail buildJobDetail(Class<? extends Job> clazz, String name) {
        LOGGER.info("Création d'un nouveau job " + name);
        return JobBuilder.newJob(clazz).withIdentity(name).storeDurably().build();
    }

    /**
     * Trigger du job à scheduler
     * @param jobDetail JobDetail du job à executer
     * @param name Nom du trigger
     * @param cronExpression Expression au format CRON
     */
    public Trigger buildJobTrigger(JobDetail jobDetail, String name, String cronExpression) {
        LOGGER.info("Création d'un nouveau trigger " + name + " avec l'expression " + cronExpression);
        return TriggerBuilder.newTrigger().forJob(jobDetail)
                .withIdentity(name)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }

    /**
     * Création ou modification d'un job existant
     * @param jobDetail Job à executer
     * @param trigger Trigger pour le job
     * @throws SchedulerException
     */
    public void startOrUpdateScheduledJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        if (scheduler.getJobDetail(jobDetail.getKey()) == null) {
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        } else {
            scheduler.rescheduleJob(trigger.getKey(), trigger);
        }
    }

    /**
     * Delete d'un job existant
     * @param jobKey Clé du job à supprimer
     */
    public void deleteExistingJob(String jobKey) throws SchedulerException {
        scheduler.deleteJob(new JobKey(jobKey));
    }
}