package mc.gouv.xaf.back.data.dao;

import java.util.Optional;
import mc.gouv.xaf.back.data.entity.DemarchesBO;
import org.springframework.data.repository.CrudRepository;

/**
 * @author qdeme
 */
public interface DemarchesRepository extends CrudRepository<DemarchesBO, String> {

    Optional<DemarchesBO> findTopBy();
}
