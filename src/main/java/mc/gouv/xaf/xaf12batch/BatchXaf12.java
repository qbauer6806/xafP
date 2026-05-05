package mc.gouv.xaf.xaf12batch;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableFeignClients(basePackages = { "mc.gouv.xaf.xaf12batch.logon" })
public class BatchXaf12 implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchXaf12.class);

    private final JobLauncher jobLauncher;
    private final Job batchJob;
    private final Job duplicateFilesJob;
    private final ConfigurableApplicationContext context;

    @Value("${batch.steps:}")
    private String stepsToRun;

    public BatchXaf12(JobLauncher jobLauncher, @Qualifier("batchJob") Job batchJob,
            @Qualifier("duplicateFilesJob") Job duplicateFilesJob, ConfigurableApplicationContext context) {
        this.jobLauncher = jobLauncher;
        this.batchJob = batchJob;
        this.duplicateFilesJob = duplicateFilesJob;
        this.context = context;
    }

    public static void main(String[] args) {
        SpringApplication.run(BatchXaf12.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            Set<String> stepsSet = getStepsSet();

            if (shouldRunBatchJob(stepsSet)) {
                runJob("batchJob", batchJob, buildJobParameters());
            }

            if (shouldRunDuplicateFilesJob(stepsSet)) {
                runJob("duplicateFilesJob", duplicateFilesJob, buildJobParameters());
            }

            LOGGER.info("Traitement terminé.");
        } finally {
            LOGGER.info("Arrêt de l'application...");
            SpringApplication.exit(context, () -> 0);
        }
    }

    private void runJob(String jobName, Job job, JobParameters parameters) {
        try {
            LOGGER.info("Lancement du {}...", jobName);
            jobLauncher.run(job, parameters);
            LOGGER.info("{} terminé avec succès.", jobName);
        } catch (JobInstanceAlreadyCompleteException e) {
            LOGGER.warn("{} déjà terminé, non relancé.", jobName);
        } catch (Exception e) {
            LOGGER.error("Erreur pendant l'exécution du {} : ", jobName, e);
        }
    }

    private JobParameters buildJobParameters() {
        if (stepsToRun == null || stepsToRun.isBlank()) {
            return new JobParameters();
        }

        return new JobParametersBuilder().addString("batch.steps", stepsToRun)
                .addLong("run.id", System.currentTimeMillis()).toJobParameters();
    }

    private Set<String> getStepsSet() {
        if (stepsToRun == null || stepsToRun.isBlank()) {
            return null;
        }

        return Arrays.stream(stepsToRun.split(",")).map(String::trim).filter(step -> !step.isEmpty())
                .collect(Collectors.toSet());
    }

    private boolean shouldRunBatchJob(Set<String> stepsSet) {
        if (stepsSet == null) {
            return true;
        }

        return stepsSet.contains("files") || stepsSet.contains("complementsFiles") || stepsSet.contains("demandes")
                || stepsSet.contains("agents") || stepsSet.contains("usagers") || stepsSet.contains("resetMarqueurs")
                || stepsSet.contains("migrateCommentaireBpm");
    }

    private boolean shouldRunDuplicateFilesJob(Set<String> stepsSet) {
        return stepsSet == null || stepsSet.contains("duplicateFiles");
    }
}
