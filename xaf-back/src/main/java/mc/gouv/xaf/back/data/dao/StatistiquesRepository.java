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

    @Query("SELECT new mc.gouv.xaf.back.data.model.StatistiqueSubsetDTO(s2.identifiantDemande, s2.statutPublic, s2.date,s1.date, s1.origine) "
            + "FROM StatistiqueBO s1 " + "JOIN StatistiqueBO s2 ON s1.demandeId = s2.demandeId "
            + "WHERE s1.statutPublic = 'SUPPRIMEE' " + "AND s1.date between :startDate AND :endDate "
            + "AND s2.statutPublic IN :statutValideOuRefuse")
    List<StatistiqueSubsetDTO> findAllBetweenDates(@Param("statutValideOuRefuse") List<String> statutValideOuRefuse,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
