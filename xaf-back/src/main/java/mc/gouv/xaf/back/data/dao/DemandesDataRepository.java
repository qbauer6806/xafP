package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesDataBO;

/**
 * @author qdeme
 *
 */
public interface DemandesDataRepository extends CrudRepository<DemandesDataBO, Integer> {

    public DemandesDataBO findByFkDemandesPkDemandesAndKey(Integer fkDemandes, String key);
    
    public List<DemandesDataBO> findByFkDemandesPkDemandes(Integer fkDemandes);
    
}
