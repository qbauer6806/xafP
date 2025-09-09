package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.Set;

public interface CommandeDemandeArticleRepository extends JpaRepository<CommandeDemandeArticleBO, Long> {

    void deleteByPkCommandesDemandesArticlesIn(Set<Integer> ids);

}
