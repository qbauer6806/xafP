package mc.gouv.af.data.dao;

import java.util.List;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.data.entity.RechercheChampConfigBo;

@Conditional(IndexationEnabledCondition.class)
public interface RechercheChampConfigRepository extends CrudRepository<RechercheChampConfigBo, Integer> {

    RechercheChampConfigBo findByCle(String cle);

    List<RechercheChampConfigBo> findByCategorieId(Integer id);

    List<RechercheChampConfigBo> findByEnabled(boolean enabled);

}
