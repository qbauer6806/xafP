package mc.gouv.xaf.back.service.scheduling;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandeJobRepository;
import mc.gouv.xaf.back.data.entity.DemandeJobBO;
import mc.gouv.xaf.shared.enums.JobStatutsEnum;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

import static mc.gouv.xaf.back.service.scheduling.JobLogCaptureService.MAX_LOG_LENGTH;

/**
 * Listener Quartz qui intercepte l'exécution des jobs pour :
 * - Capturer les logs émis pendant l'exécution
 * - Mettre à jour le statut et les logs dans la table DEM_JOBS
 */
@Component
@RequiredArgsConstructor
public class GouvJobListener implements JobListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvJobListener.class);

    public static final String DEMANDE_JOB_ID_KEY = "demandeJobId";

    private final DemandeJobRepository demandeJobRepository;
    private final JobLogCaptureService logCaptureService;

    @Override
    public String getName() {
        return "GouvJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String demandeJobId = context.getMergedJobDataMap().getString(DEMANDE_JOB_ID_KEY);
        if (demandeJobId != null) {
            LOGGER.info("Début d'exécution du job Quartz {} (demandeJobId={})",
                    context.getJobDetail().getKey().getName(), demandeJobId);
            // Démarrer la capture des logs et positionner le MDC pour le thread courant
            logCaptureService.startCapture(demandeJobId);
            MDC.put(JobLogCaptureAppender.MDC_KEY, demandeJobId);
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        String demandeJobId = context.getMergedJobDataMap().getString(DEMANDE_JOB_ID_KEY);
        if (demandeJobId != null) {
            logCaptureService.stopCapture(demandeJobId);
            MDC.remove(JobLogCaptureAppender.MDC_KEY);
        }
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String demandeJobId = context.getMergedJobDataMap().getString(DEMANDE_JOB_ID_KEY);
        if (demandeJobId == null) {
            return;
        }

        try {
            // Récupérer les logs capturés
            String capturedLogs = logCaptureService.stopCapture(demandeJobId);
            MDC.remove(JobLogCaptureAppender.MDC_KEY);

            Integer jobId = Integer.parseInt(demandeJobId);
            Optional<DemandeJobBO> jobOpt = demandeJobRepository.findById(jobId);

            if (jobOpt.isPresent()) {
                DemandeJobBO job = jobOpt.get();
                job.setDateDernModif(new Date());

                if (jobException != null) {
                    // Job en erreur
                    job.setStatut(JobStatutsEnum.ERROR);
                    StringBuilder errorMsg = new StringBuilder();
                    errorMsg.append("Erreur lors de l'exécution du job Quartz.\n");
                    if (jobException.getMessage() != null) {
                        errorMsg.append("Message: ").append(jobException.getMessage()).append("\n");
                    }
                    if (jobException.getCause() != null && jobException.getCause().getMessage() != null) {
                        errorMsg.append("Cause: ").append(jobException.getCause().getMessage()).append("\n");
                    }
                    if (!capturedLogs.isEmpty()) {
                        errorMsg.append("\n--- Logs d'exécution ---\n").append(capturedLogs);
                    }
                    job.setMsg(truncate(errorMsg.toString()));
                    LOGGER.error("Job Quartz {} terminé en erreur (demandeJobId={})",
                            context.getJobDetail().getKey().getName(), demandeJobId, jobException);
                } else {
                    // Job réussi
                    job.setStatut(JobStatutsEnum.SUCCEEDED);
                    StringBuilder successMsg = new StringBuilder();
                    successMsg.append("Job Quartz exécuté avec succès.\n");
                    long durationMs = context.getJobRunTime();
                    successMsg.append("Durée d'exécution: ").append(formatDuration(durationMs)).append("\n");
                    if (!capturedLogs.isEmpty()) {
                        successMsg.append("\n--- Logs d'exécution ---\n").append(capturedLogs);
                    } else {
                        successMsg.append("(Aucun log capturé)");
                    }
                    job.setMsg(truncate(successMsg.toString()));
                    LOGGER.info("Job Quartz {} terminé avec succès (demandeJobId={})",
                            context.getJobDetail().getKey().getName(), demandeJobId);
                }

                demandeJobRepository.save(job);
            }
        } catch (Exception e) {
            LOGGER.error("Erreur dans GouvJobListener lors de la mise à jour du job {}", demandeJobId, e);
        }
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() > MAX_LOG_LENGTH ? text.substring(0, MAX_LOG_LENGTH) + "\n... (tronqué)" : text;
    }

    private String formatDuration(long durationMs) {
        if (durationMs < 1000) {
            return durationMs + " ms";
        } else if (durationMs < 60000) {
            return String.format("%.1f s", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return String.format("%d min %d s", minutes, seconds);
        }
    }
}
