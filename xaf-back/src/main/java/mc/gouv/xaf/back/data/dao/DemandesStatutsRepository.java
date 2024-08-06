package mc.gouv.xaf.back.data.dao;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;

/**
 * 
 * @author qdeme
 *
 */
public interface DemandesStatutsRepository extends CrudRepository<DemandesStatutsBO, Integer> {
	DemandesStatutsBO findByFkDemandesPkDemandesAndName(Integer fkDemandes, String name);

}
