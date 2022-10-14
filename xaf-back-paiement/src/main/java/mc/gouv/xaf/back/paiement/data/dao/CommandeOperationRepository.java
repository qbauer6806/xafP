package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeOperationBO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeOperationRepository extends JpaRepository<CommandeOperationBO, String> {
}

