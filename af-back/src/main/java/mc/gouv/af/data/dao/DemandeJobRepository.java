package mc.gouv.af.data.dao;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.data.entity.DemandeJobBO;

@Conditional(IndexationEnabledCondition.class)
public interface DemandeJobRepository extends CrudRepository<DemandeJobBO, Integer> {

    Page<DemandeJobBO> findAll(Pageable pageable);

}
