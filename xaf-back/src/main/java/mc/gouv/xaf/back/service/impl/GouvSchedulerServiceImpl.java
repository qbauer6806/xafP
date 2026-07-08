package mc.gouv.xaf.back.service.impl;

import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.GouvSchedulerService;
import mc.gouv.xaf.back.service.scheduling.GouvJobListener;
import mc.gouv.xaf.back.service.scheduling.JobLogCaptureAppender;
import mc.gouv.xaf.back.service.scheduling.JobLogCaptureService;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

@Component
@RequiredArgsConstructor
public class GouvSchedulerServiceImpl implements GouvSchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvSchedulerServiceImpl.class);

    private final Scheduler scheduler;
    private final GouvJobListener gouvJobListener;
    private final JobLogCaptureService jobLogCaptureService;

    /**
     * Initialise le listener Quartz et l'appender de capture des logs au démarrage.
     */
    @PostConstruct
    public void init() throws SchedulerException {
        // Enregistrer le JobListener sur le scheduler Quartz
        scheduler.getListenerManager().addJobListener(gouvJobListener);
        LOGGER.info("GouvJobListener enregistré sur le scheduler Quartz");

        // Enregistrer l'appender de capture des logs sur le root logger Logback
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        JobLogCaptureAppender appender = new JobLogCaptureAppender();
        appender.setCaptureService(jobLogCaptureService);
        appender.setContext(loggerContext);
        appender.setName("JOB_LOG_CAPTURE");
        appender.start();
        loggerContext.getLogger(ROOT_LOGGER_NAME).addAppender(appender);
        LOGGER.info("JobLogCaptureAppender enregistré sur le root logger");
    }

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

    @Override
    public java.util.List<String> getAllJobNames() throws SchedulerException {
        return scheduler.getJobKeys(org.quartz.impl.matchers.GroupMatcher.anyJobGroup())
                .stream()
                .map(jobKey -> jobKey.getName())
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void triggerJob(String jobName) throws SchedulerException {
        scheduler.triggerJob(new JobKey(jobName));
    }

    @Override
    public void triggerJob(String jobName, Integer demandeJobId) throws SchedulerException {
        if (demandeJobId != null) {
            JobDataMap dataMap = new JobDataMap();
            dataMap.put(GouvJobListener.DEMANDE_JOB_ID_KEY, String.valueOf(demandeJobId));
            scheduler.triggerJob(new JobKey(jobName), dataMap);
        } else {
            triggerJob(jobName);
        }
    }

}
