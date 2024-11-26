package mc.gouv.xaf.back.service.impl;

import mc.gouv.xaf.back.service.GouvSchedulerService;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
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
     *
     * @param clazz
     *         Class du job à executer
     * @param name
     *         Nom du job
     */
    public JobDetail buildJobDetail(Class<? extends Job> clazz, String name) {
        LOGGER.info("Création d'un nouveau job {}", name);
        return JobBuilder.newJob(clazz).withIdentity(name).storeDurably().build();
    }

    /**
     * Trigger du job à scheduler
     *
     * @param jobDetail
     *         JobDetail du job à executer
     * @param name
     *         Nom du trigger
     * @param cronExpression
     *         Expression au format CRON
     */
    public Trigger buildJobTrigger(JobDetail jobDetail, String name, String cronExpression) {
        LOGGER.info("Création d'un nouveau trigger {} avec l'expression {}", name, cronExpression);
        return TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(name)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
    }

    /**
     * Création ou modification d'un job existant
     *
     * @param jobDetail
     *         Job à executer
     * @param trigger
     *         Trigger pour le job
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
     * Récupération d'un trigger existant
     *
     * @param trigger
     *         Trigger à récupérer
     * @throws SchedulerException
     */
    public Trigger getTrigger(String trigger) throws SchedulerException {
        TriggerKey triggerKey = new TriggerKey(trigger);
        return scheduler.getTrigger(triggerKey);
    }

    /**
     * Delete d'un job existant
     *
     * @param jobKey
     *         Clé du job à supprimer
     */
    public void deleteExistingJob(String jobKey) throws SchedulerException {
        scheduler.deleteJob(new JobKey(jobKey));
    }

}

