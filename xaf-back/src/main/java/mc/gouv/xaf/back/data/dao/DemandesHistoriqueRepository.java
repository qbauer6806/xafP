package mc.gouv.xaf.back.data.dao;

import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandesHistoriqueBO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author qdeme
 */
public interface DemandesHistoriqueRepository extends CrudRepository<DemandesHistoriqueBO, Integer> {

    List<DemandesHistoriqueBO> findByFkDemandesPkDemandes(Integer pkDemandes);

    @Modifying
    @Transactional
    void deleteByFkDemandesPkDemandes(Integer pkDemandes);
}
