package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author qdeme
 */
public interface DemandesStatutsRepository extends JpaRepository<DemandesStatutsBO, Integer> {

    DemandesStatutsBO findByFkDemandesPkDemandesAndName(Integer fkDemandes, String name);
}
