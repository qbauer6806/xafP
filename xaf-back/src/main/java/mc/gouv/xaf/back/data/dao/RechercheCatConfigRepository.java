package mc.gouv.xaf.back.data.dao;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.entity.RechercheCatConfigBO;

@Conditional(IndexationEnabledCondition.class)
public interface RechercheCatConfigRepository extends CrudRepository<RechercheCatConfigBO, Integer> {

    RechercheCatConfigBO findByLibelle(String libelle);

}
