package mc.gouv.xaf.xaf12batch;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManagerFactory;
import mc.gouv.xaf.xaf12batch.dto.DemandeBO;
import mc.gouv.xaf.xaf12batch.dto.DemandesComplementsFilesBO;
import mc.gouv.xaf.xaf12batch.dto.DemandesFilesBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    private DemandeFileTransformer demandeFileTransformer;

    @Autowired
    private DemandeTransformer demandeTransformer;

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchConfig.class);

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public JpaPagingItemReader<DemandesFilesBO> filesReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<DemandesFilesBO>()
                .name("demandeFileReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT d FROM DemandesFilesBO d ORDER BY d.pkDemandesFiles ASC")
                .pageSize(10)
                .build();
    }

    @Bean
    public JpaPagingItemReader<DemandesComplementsFilesBO> complementsFilesReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<DemandesComplementsFilesBO>()
                .name("demandeComplementFileReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT d FROM DemandesComplementsFilesBO d ORDER BY d.pkDemandesComplementsFiles ASC")
                .pageSize(10)
                .build();
    }

    @Bean
    public JpaPagingItemReader<DemandeBO> demandesReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<DemandeBO>()
                .name("demandesReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT d FROM DemandeBO d ORDER BY d.pkDemandes ASC")
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<DemandesFilesBO, DemandesFilesBO> filesProcessor() {
        return file -> {
            LOGGER.info("Traitement du file ID {}", file.getPkDemandesFiles());
            String url = file.getUrl();
            if (url != null && (url.endsWith(".doc") || url.endsWith(".docx") || url.endsWith(".rtf") || url.endsWith(".pdf"))) {
                String text = demandeFileTransformer.getFileText(url);
                if (text.length() > 100000) {
                    LOGGER.info("Contenu trop long, fichier ignoré");
                } else {
                    file.setContenu(text);
                }
            }
            return file;
        };
    }

    @Bean
    public ItemProcessor<DemandesComplementsFilesBO, DemandesComplementsFilesBO> complementsFilesProcessor() {
        return file -> {
            LOGGER.info("Traitement de complementFile ID {}", file.getPkDemandesComplementsFiles());
            String url = file.getUrl();
            if (url != null && (url.endsWith(".doc") || url.endsWith(".docx") || url.endsWith(".rtf") || url.endsWith(".pdf"))) {
                String text = demandeFileTransformer.getFileText(url);
                if (text.length() > 100000) {
                    LOGGER.info("Contenu trop long, fichier ignoré");
                } else {
                    file.setContenu(text);
                }
            }
            return file;
        };
    }

    @Bean
    public ItemProcessor<DemandeBO, DemandeBO> demandesProcessor() {
        return demande -> {
            LOGGER.info("Traitement de la demande ID {}", demande.getPkDemandes());
            if (demande.getConfig() != null) {
                JsonNode contenuTrad = demande.getContenuTrad();
                demandeTransformer.setContenuTrad(contenuTrad, demande.getConfig().getContenu());
                demande.setContenuTrad(contenuTrad);
            }
            return demande;
        };
    }

    @Bean
    public JpaItemWriter<DemandesFilesBO> filesWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<DemandesFilesBO>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public JpaItemWriter<DemandesComplementsFilesBO> complementsFilesWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<DemandesComplementsFilesBO>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public JpaItemWriter<DemandeBO> demandesWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<DemandeBO>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step filesStep(EntityManagerFactory entityManagerFactory) {
        return new StepBuilder("filesStep", jobRepository)
                .<DemandesFilesBO, DemandesFilesBO>chunk(10, transactionManager)
                .reader(filesReader(entityManagerFactory))
                .processor(filesProcessor())
                .writer(filesWriter(entityManagerFactory))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step complementsFilesStep(EntityManagerFactory entityManagerFactory) {
        return new StepBuilder("complementsFilesStep", jobRepository)
                .<DemandesComplementsFilesBO, DemandesComplementsFilesBO>chunk(10, transactionManager)
                .reader(complementsFilesReader(entityManagerFactory))
                .processor(complementsFilesProcessor())
                .writer(complementsFilesWriter(entityManagerFactory))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step demandesStep(EntityManagerFactory entityManagerFactory) {
        return new StepBuilder("demandesStep", jobRepository)
                .<DemandeBO, DemandeBO>chunk(10, transactionManager)
                .reader(demandesReader(entityManagerFactory))
                .processor(demandesProcessor())
                .writer(demandesWriter(entityManagerFactory))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Job batchJob() {
        return new JobBuilder("batchJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(filesStep(null))  // Premier Step pour la première table
                .next(complementsFilesStep(null))   // Deuxième Step pour la deuxième table
                //                .next(demandesStep(null))   // Deuxième Step pour la deuxième table
                .build();
    }

}
