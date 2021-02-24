package mc.gouv.xaf.scheduling;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service permettant une gestion des jobs Quartz
 * @author mpavone.ext
 */
public interface GouvSchedulerService {

    JobDetail buildJobDetail(Class<? extends Job> clazz, String name);

    Trigger buildJobTrigger(JobDetail jobDetail, String name, String cronExpression);

    void startScheduledJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException;
}