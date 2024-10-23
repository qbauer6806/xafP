package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import org.springframework.data.repository.CrudRepository;

/**
 * @author qdeme
 */
public interface DemandesStatutsRepository extends CrudRepository<DemandesStatutsBO, Integer> {

    DemandesStatutsBO findByFkDemandesPkDemandesAndName(Integer fkDemandes, String name);
}
