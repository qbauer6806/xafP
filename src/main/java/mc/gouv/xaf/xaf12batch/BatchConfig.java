package mc.gouv.xaf.xaf12batch;

import javax.sql.DataSource;
import mc.gouv.xaf.xaf12batch.dto.DemandeDTO;
import mc.gouv.xaf.xaf12batch.dto.DemandeEsDTO;
import mc.gouv.xaf.xaf12batch.reader.DemandeEsItemReader;
import mc.gouv.xaf.xaf12batch.writer.CustomJdbcBatchItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DemandeEsItemReader demandeEsItemReader;

    @Bean
    public Job migrateDataJob() {
        return jobBuilderFactory.get("migrateDataJob")
                .start(migrateDemandeEsDTOStep())
                .build();
    }

    @Bean
    public Step migrateDemandeEsDTOStep() {
        return stepBuilderFactory.get("migrateDemandeEsDTOStep")
                .<DemandeEsDTO, DemandeDTO>chunk(100)
                .reader(demandeEsItemReader)
                .processor(demandeEsDTOProcessor())
                .writer(demandeWriter())
                .build();
    }


    @Bean
    public ItemProcessor<DemandeEsDTO, DemandeDTO> demandeEsDTOProcessor() {
        return demandeEsDTO -> {
            System.out.println("demandeEsDTOProcessor");
            System.out.println(demandeEsDTO);
            DemandeDTO demande = new DemandeDTO();
            demande.setPkDemandes(demandeEsDTO.getPkDemandes());
            demande.setCode(demandeEsDTO.getDernierStatut().getCode());
            demande.setLibelle(demandeEsDTO.getDernierStatut().getLibelle());
            return demande;
        };
    }

    @Bean
    public JdbcBatchItemWriter<DemandeDTO> delegateDemandeWriter() {
        return new JdbcBatchItemWriterBuilder<DemandeDTO>()
                .dataSource(dataSource)
                .sql("UPDATE dem_demandes_statuts SET name = :code, libelle = :libelle FROM dem_demandes WHERE dem_demandes_statuts.pk_demandesstatuts = dem_demandes.fk_dernier_statut AND dem_demandes.pk_demandes = :pkDemandes")
                .beanMapped()
                .build();
    }

    @Bean
    public CustomJdbcBatchItemWriter<DemandeDTO> demandeWriter() {
        return CustomJdbcBatchItemWriter.customJdbcBatchItemWriter(delegateDemandeWriter());
    }

}
