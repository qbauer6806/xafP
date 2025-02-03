package mc.gouv.xaf.xaf12batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableFeignClients(basePackages = { "mc.gouv.xaf.xaf12batch.logon" })
public class BatchXaf12 implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchXaf12.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Autowired
    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        // Initialise le mapping des packages
        Map<String, String> packageReplacement = new HashMap<>();
        packageReplacement.put("mc.gouv.af.back.bpm.model", "mc.gouv.xaf.xaf12batch.bpm");
        packageReplacement.put("mc.gouv.xaf.back.bpm.model", "mc.gouv.xaf.xaf12batch.bpm");

        // Crée le CustomClassLoader
        CustomClassLoader customClassLoader = new CustomClassLoader(BatchXaf12.class.getClassLoader(),
                packageReplacement);

        // Défini le ClassLoader par défaut
        Thread.currentThread().setContextClassLoader(customClassLoader);

        // Démarrage de l'application Spring
        SpringApplication.run(BatchXaf12.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            // Lancer le job
            LOGGER.info("Lancement du job...");
            jobLauncher.run(job, new JobParameters());
            LOGGER.info("Job terminé avec succès.");
        } catch (Exception e) {
            LOGGER.error("Erreur pendant l'exécution du job : ", e);
        } finally {
            // Arrêter l'application après le job
            LOGGER.info("Arrêt de l'application...");
            SpringApplication.exit(context, () -> 0);
        }
    }
}
