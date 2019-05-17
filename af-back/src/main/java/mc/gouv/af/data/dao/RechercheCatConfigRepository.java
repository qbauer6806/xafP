package mc.gouv.af.data.dao;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.data.entity.RechercheCatConfigBo;

@Conditional(IndexationEnabledCondition.class)
public interface RechercheCatConfigRepository extends CrudRepository<RechercheCatConfigBo, Integer> {

    RechercheCatConfigBo findByLibelle(String libelle);

}
