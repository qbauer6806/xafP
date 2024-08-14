package mc.gouv.xaf.xaf12batch;

import jakarta.persistence.EntityManagerFactory;
import mc.gouv.xaf.xaf12batch.dto.DemandesFilesBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchConfig.class);

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }
    @Bean
    public JpaPagingItemReader<DemandesFilesBO> reader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<DemandesFilesBO>()
                .name("demandeFileReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT d FROM DemandesFilesBO d ORDER BY d.pkDemandesFiles ASC")
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<DemandesFilesBO, DemandesFilesBO> processor() {
        return file -> {
            LOGGER.info("Traitement de l'élément ID {}", file.getPkDemandesFiles());
            String url = file.getUrl();
            if (url != null && (url.endsWith(".doc") || url.endsWith(".docx") || url.endsWith(".rtf") || url.endsWith(".pdf"))) {
                file.setContenu(demandeFileTransformer.getFileText(url));
            }
            return file;
        };
    }

    @Bean
    public JpaItemWriter<DemandesFilesBO> writer(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<DemandesFilesBO>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step step1(EntityManagerFactory entityManagerFactory) {
        return new StepBuilder("step1", jobRepository)
                .<DemandesFilesBO, DemandesFilesBO>chunk(10, transactionManager)
                .reader(reader(entityManagerFactory))
                .processor(processor())
                .writer(writer(entityManagerFactory))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Job importUserJob(@Qualifier("step1") Step step1) {
        return new JobBuilder("importUserJob", jobRepository)
                .incrementer(new org.springframework.batch.core.launch.support.RunIdIncrementer())
                .start(step1)
                .build();
    }

}
