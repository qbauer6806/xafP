package mc.gouv.xaf.back.data.dao;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.entity.DemandeJobBO;

@Conditional(IndexationEnabledCondition.class)
public interface DemandeJobRepository extends CrudRepository<DemandeJobBO, Integer> {

    Page<DemandeJobBO> findAll(Pageable pageable);

}
