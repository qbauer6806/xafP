package mc.gouv.xaf.data.dao;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.data.entity.RechercheCatConfigBo;

@Conditional(IndexationEnabledCondition.class)
public interface RechercheCatConfigRepository extends CrudRepository<RechercheCatConfigBo, Integer> {

    RechercheCatConfigBo findByLibelle(String libelle);

}
