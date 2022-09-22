package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeRepository extends JpaRepository<CommandeBO, Long> {

    CommandeBO findByMoyenPaiementPkMoyensPaiements(String pkMoyenPaiement);
}
