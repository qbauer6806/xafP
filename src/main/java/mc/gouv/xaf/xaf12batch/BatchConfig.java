package mc.gouv.xaf.xaf12batch;

import javax.sql.DataSource;
import mc.gouv.xaf.xaf12batch.dto.DemandeDTO;
import mc.gouv.xaf.xaf12batch.dto.DemandeEsDTO;
import mc.gouv.xaf.xaf12batch.dto.DemandeFileDTO;
import mc.gouv.xaf.xaf12batch.dto.DemandeFileEsDTO;
import mc.gouv.xaf.xaf12batch.reader.DemandeEsFileItemReader;
import mc.gouv.xaf.xaf12batch.reader.DemandeEsItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

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

    @Autowired
    private DemandeEsFileItemReader demandeEsFileItemReader;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Bean
    public Job migrateDataJob() {
        return jobBuilderFactory.get("migrateDataJob")
                .start(migrateDemandeEsDTOStep())
                .next(migrateDemandeFileEsDTOStep())
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
    public Step migrateDemandeFileEsDTOStep() {
        return stepBuilderFactory.get("migrateDemandeFileEsDTOStep")
                .<DemandeFileEsDTO, DemandeFileDTO>chunk(100)
                .reader(demandeEsFileItemReader)
                .processor(demandeFileEsDTOProcessor())
                .writer(demandeFileWriter())
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
    public ItemProcessor<DemandeFileEsDTO, DemandeFileDTO> demandeFileEsDTOProcessor() {
        return demandeFileEsDTO -> {
            System.out.println("demandeFileEsDTOProcessor");
            System.out.println(demandeFileEsDTO);
            DemandeFileDTO demandeFile = new DemandeFileDTO();
            demandeFile.setContent(demandeFileEsDTO.getContent());
            demandeFile.setTypeFichier(demandeFileEsDTO.getTypeFichier());
            demandeFile.setUrl(demandeFileEsDTO.getUrl());
            return demandeFile;
        };
    }

    @Bean
    public JdbcBatchItemWriter<DemandeDTO> demandeWriter() {
        return new JdbcBatchItemWriterBuilder<DemandeDTO>()
                .dataSource(dataSource)
                .sql("UPDATE dem_demandes_statuts SET name = :code, libelle = :libelle FROM dem_demandes WHERE dem_demandes_statuts.pk_demandesstatuts = dem_demandes.fk_dernier_statut AND dem_demandes.pk_demandes = :pkDemandes")
                .beanMapped()
                .build();
    }

    @Bean
    public ItemWriter<DemandeFileDTO> demandeFileWriter() {
        return items -> {
            for (DemandeFileDTO item : items) {
                int updatedRows1 = jdbcTemplate.update(
                        "UPDATE dem_demandes_files SET contenu = ? WHERE url = ? AND ? = 'PIECE_JOINTE'",
                        item.getContent(), item.getUrl(), item.getTypeFichier());

                int updatedRows2 = jdbcTemplate.update(
                        "UPDATE dem_demandes_complements_files SET contenu = ? WHERE url = ? AND ? = 'COMPLEMENT'",
                        item.getContent(), item.getUrl(), item.getTypeFichier());

                if (updatedRows1 == 0 && updatedRows2 == 0) {
                    System.err.println("No rows were updated for item: " + item);
                }
            }
        };
    }
}
