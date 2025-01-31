package mc.gouv.xaf.xaf12batch;

import jakarta.persistence.EntityManagerFactory;
import mc.gouv.xaf.xaf12batch.dto.DemandeBO;
import mc.gouv.xaf.xaf12batch.dto.DemandesAgentsBO;
import mc.gouv.xaf.xaf12batch.dto.DemandesComplementsFilesBO;
import mc.gouv.xaf.xaf12batch.dto.DemandesFilesBO;
import mc.gouv.xaf.xaf12batch.dto.DemandesUsagersBO;
import mc.gouv.xaf.xaf12batch.gichuni.DemandesUsagersTransformer;
import mc.gouv.xaf.xaf12batch.gichuni.GichuniUsagerDTO;
import mc.gouv.xaf.xaf12batch.logon.UtilisateursCache;
import mc.gouv.xaf.xaf12batch.logon.dto.User;
import mc.gouv.xboot.caching.GouvCache;
import org.activiti.compatibility.spring.DefaultFlowable5SpringCompatibilityHandler;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
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
    private ResetMarqueursTasklet resetMarqueursTasklet;

    @Autowired
    private MigrateCommentairesBpmTasklet migrateCommentairesBpmTasklet;

    @Autowired
    private DemandeFileTransformer demandeFileTransformer;

    @Autowired
    private DemandeTransformer demandeTransformer;

    @Autowired
    private DemandesAgentsTransformer demandesAgentsTransformer;

    @Autowired
    private DemandesUsagersTransformer demandesUsagersTransformer;

    @Autowired
    private UtilisateursCache utilisateursCache;

    @Autowired
    private GouvCache<Integer, GichuniUsagerDTO> usagersCache;

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
    public JpaPagingItemReader<DemandesAgentsBO> agentsReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<DemandesAgentsBO>()
                .name("agentsReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT d FROM DemandesAgentsBO d ORDER BY d.id ASC")
                .pageSize(10)
                .build();
    }

    @Bean
    public JpaPagingItemReader<DemandesUsagersBO> usagersReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<DemandesUsagersBO>()
                .name("agentsReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT d FROM DemandesUsagersBO d WHERE d.id < 1000000000 ORDER BY d.id ASC")
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
                if (text.length() > 100000 || text.startsWith("{\"errors\":")) {
                    LOGGER.warn("Fichier {} : contenu trop long ou fichier inexistant, ignoré", file.getPkDemandesFiles());
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
                if (text.length() > 100000 || text.startsWith("{\"errors\":")) {
                    LOGGER.warn("Fichier {} : contenu trop long ou fichier inexistant, ignoré",  file.getPkDemandesComplementsFiles());
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
                // changer les valeur/valeurExtra
                demandeTransformer.changeChoixAdditionnel(demande.getContenu());
                demandeTransformer.changeChoixAdditionnel(demande.getContenuTrad());
                // changer les choix multiple avec le nouveau format
                demandeTransformer.changeChoixMultiple(demande.getConfig().getContenu(), demande.getContenu());
                demandeTransformer.changeChoixMultiple(demande.getConfig().getContenu(), demande.getContenuTrad());
                // changer les types complexes dans les tableaux
                demandeTransformer.changeTableauComplexe(demande.getConfig().getContenu(), demande.getContenu());
                demandeTransformer.changeTableauComplexe(demande.getConfig().getContenu(), demande.getContenuTrad());
                // transformer les clés qui sont dans contenuTrad en libellé
                demandeTransformer.setContenuTrad(demande.getContenuTrad(), demande.getConfig().getContenu());
            }
            return demande;
        };
    }

    @Bean
    public ItemProcessor<DemandesAgentsBO, DemandesAgentsBO> agentsProcessor() {
        return agent -> {
            LOGGER.info("Traitement de l'agent ID {}", agent.getId());
            User user = utilisateursCache.get(String.valueOf(agent.getId()));
            demandesAgentsTransformer.user2Bo(user, agent);
            return agent;
        };
    }

    @Bean
    public ItemProcessor<DemandesUsagersBO, DemandesUsagersBO> usagersProcessor() {
        return usagerBo -> {
            LOGGER.info("Traitement de l'usager ID {}", usagerBo.getId());
            GichuniUsagerDTO usager = usagersCache.get(usagerBo.getId());
            if (usager == null) {
                LOGGER.warn("Usager ID {} non trouvé", usagerBo.getId());
            } else {
                demandesUsagersTransformer.user2Bo(usager, usagerBo);
            }
            return usagerBo;
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
    public JpaItemWriter<DemandesAgentsBO> agentsWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<DemandesAgentsBO>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public JpaItemWriter<DemandesUsagersBO> usagersWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<DemandesUsagersBO>()
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
    public Step agentsStep(EntityManagerFactory entityManagerFactory) {
        return new StepBuilder("agentsStep", jobRepository)
                .<DemandesAgentsBO, DemandesAgentsBO>chunk(10, transactionManager)
                .reader(agentsReader(entityManagerFactory))
                .processor(agentsProcessor())
                .writer(agentsWriter(entityManagerFactory))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step usagersStep(EntityManagerFactory entityManagerFactory) {
        return new StepBuilder("usagersStep", jobRepository)
                .<DemandesUsagersBO, DemandesUsagersBO>chunk(10, transactionManager)
                .reader(usagersReader(entityManagerFactory))
                .processor(usagersProcessor())
                .writer(usagersWriter(entityManagerFactory))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step resetMarqueursStep() {
        return new StepBuilder("resetMarqueursStep", jobRepository).tasklet(resetMarqueursTasklet, transactionManager)
                .allowStartIfComplete(true).build();
    }

    @Bean
    public Step migrateCommentaireBpmStep() {
        return new StepBuilder("migrateCommentaireBpmStep", jobRepository).tasklet(migrateCommentairesBpmTasklet,
                transactionManager).allowStartIfComplete(true).build();
    }

    @Bean
    public Job batchJob() {
        return new JobBuilder("batchJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(filesStep(null))
                .next(complementsFilesStep(null))
                .next(demandesStep(null))
                .next(agentsStep(null)).next(usagersStep(null)).next(resetMarqueursStep())
                .next(migrateCommentaireBpmStep())
                .build();
    }

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> enableFlowable5CompatibilityConfigurer() {
        return (SpringProcessEngineConfiguration processEngineConfiguration) -> {
            processEngineConfiguration.setFlowable5CompatibilityEnabled(true);
            processEngineConfiguration.setFlowable5CompatibilityHandlerFactory(
                    DefaultFlowable5SpringCompatibilityHandler::new);
        };

    }

}
