package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.entity.RechercheChampConfigBO;

@Conditional(IndexationEnabledCondition.class)
public interface RechercheChampConfigRepository extends CrudRepository<RechercheChampConfigBO, Integer> {

    RechercheChampConfigBO findByCle(String cle);

    List<RechercheChampConfigBO> findByCategorieId(Integer id);

    List<RechercheChampConfigBO> findByEnabled(boolean enabled);

}
