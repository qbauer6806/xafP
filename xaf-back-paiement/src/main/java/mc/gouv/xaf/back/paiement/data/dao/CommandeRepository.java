package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import org.springframework.data.repository.CrudRepository;

public interface CommandeRepository extends CrudRepository<CommandeBO, Long> {


}
