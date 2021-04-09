package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface StatistiquesRepository extends CrudRepository<StatistiqueBO, Integer> {

    List<StatistiqueBO> findByDemandeIdAndDemarcheId(Integer demandeId, String demarcheId);

    List<StatistiqueBO> findByStatutPublicAndDateBetween(String statut, Date d1, Date d2);

    StatistiqueBO findFirstByDemandeIdAndStatutPublicNotOrderByDateDesc(Integer demandeId, String statut);
}
