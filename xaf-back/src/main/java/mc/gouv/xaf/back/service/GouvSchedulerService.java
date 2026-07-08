package mc.gouv.xaf.back.service;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import java.util.List;

/**
 * Service permettant une gestion des jobs Quartz
 *
 * @author mpavone.ext
 */
public interface GouvSchedulerService {

    JobDetail buildJobDetail(Class<? extends Job> clazz, String name);

    Trigger buildJobTrigger(JobDetail jobDetail, String name, String cronExpression);

    void startOrUpdateScheduledJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException;

    Trigger getTrigger(String trigger) throws SchedulerException;

    void deleteExistingJob(String jobKey) throws SchedulerException;
    
    List<String> getAllJobNames() throws SchedulerException;

    void triggerJob(String jobName) throws SchedulerException;

    /**
     * Déclenche un job Quartz et lie l'exécution à un enregistrement DemandeJob
     * pour capturer les logs d'exécution.
     *
     * @param jobName       Nom du job à déclencher
     * @param demandeJobId  ID de l'enregistrement DemandeJob pour le suivi des logs
     */
    void triggerJob(String jobName, Integer demandeJobId) throws SchedulerException;
}
