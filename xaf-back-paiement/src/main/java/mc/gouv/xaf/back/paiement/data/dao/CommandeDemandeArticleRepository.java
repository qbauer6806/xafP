package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeDemandeArticleRepository extends JpaRepository<CommandeDemandeArticleBO, Long> {

}
