package mc.gouv.xaf.back.data.dao;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;

/**
 * @author qdeme
 *
 */
public interface DemandesComplementsRepository extends CrudRepository<DemandesComplementsBO, Integer> {

    DemandesComplementsBO findByPkDemandesComplementsAndFkDemandesPkDemandes(Integer pkDemandesComplements, Integer pkDemandes);
    
}
