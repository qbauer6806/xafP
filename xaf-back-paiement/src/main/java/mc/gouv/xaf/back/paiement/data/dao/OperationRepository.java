package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import org.springframework.data.repository.CrudRepository;

public interface OperationRepository extends CrudRepository<OperationBO, String> {
}

