package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.DemandeJobBO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface DemandeJobRepository extends CrudRepository<DemandeJobBO, Integer> {

    Page<DemandeJobBO> findAll(Pageable pageable);

}
