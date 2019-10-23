package mc.gouv.xaf.back.data.dao;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;

/**
 * @author qdeme
 *
 */
public interface DemandesComplementsRepository extends CrudRepository<DemandesComplementsBO, Integer> {

    public DemandesComplementsBO findByPkDemandesComplementsAndFkDemandesPkDemandesAndFkDemandesFkAccessDemarcheId(Integer pkDemandesComplements, Integer pkDemandes, String demarcheId);
    
}
