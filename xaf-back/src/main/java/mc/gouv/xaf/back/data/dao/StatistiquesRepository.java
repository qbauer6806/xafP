package mc.gouv.xaf.back.data.dao;

import java.util.Date;
import java.util.List;
import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import mc.gouv.xaf.back.data.model.StatistiqueSubsetDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface StatistiquesRepository extends CrudRepository<StatistiqueBO, Integer> {

    List<StatistiqueBO> findByDemandeId(Integer demandeId);

    List<StatistiqueBO> findByStatutPublicAndDateBetween(String statut, Date d1, Date d2);

    StatistiqueBO findFirstByDemandeIdAndStatutPublicNotOrderByDateDesc(Integer demandeId, String statut);

    @Query("SELECT new mc.gouv.xaf.back.data.model.StatistiqueSubsetDTO(s2.identifiantDemande, s2.statutPublic, s2.date,s1.date) "
            + "FROM StatistiqueBO s1 " + "JOIN StatistiqueBO s2 ON s1.demandeId = s2.demandeId "
            + "WHERE s1.statutPublic = 'SUPPRIMEE' " + "AND s1.date between :startDate AND :endDate "
            + "AND s2.statutPublic IN :statutValideOuRefuse "
            //#59845 : On ajoute une sous requête pour récupérer la ligne la plus récente en cas de doublon
            + "AND s2.pkStatistiques = (SELECT MAX (s3.pkStatistiques) FROM StatistiqueBO s3 WHERE s2.demandeId = s3.demandeId "
            + "AND s3.statutPublic IN :statutValideOuRefuse)")
    List<StatistiqueSubsetDTO> findAllBetweenDates(@Param("statutValideOuRefuse") List<String> statutValideOuRefuse,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
