package mc.gouv.xaf.back.service;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * Service permettant une gestion des jobs Quartz
 * @author mpavone.ext
 */
public interface GouvSchedulerService {

    JobDetail buildJobDetail(Class<? extends Job> clazz, String name);

    Trigger buildJobTrigger(JobDetail jobDetail, String name, String cronExpression);

    void startOrUpdateScheduledJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException;

    Trigger getTrigger(String trigger) throws SchedulerException;

    void deleteExistingJob(String jobKey) throws SchedulerException;
}
