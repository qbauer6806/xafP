package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StatistiquesRepository extends CrudRepository<StatistiqueBO, Integer> {

    List<StatistiqueBO> findByDemandeIdAndDemarcheId(Integer demandeId, String demarcheId);

}
