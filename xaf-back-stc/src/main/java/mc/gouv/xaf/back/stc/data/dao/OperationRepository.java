package mc.gouv.xaf.back.stc.data.dao;

import mc.gouv.xaf.back.stc.data.entity.OperationBO;
import org.springframework.data.repository.CrudRepository;

public interface OperationRepository extends CrudRepository<OperationBO, String> {
}

