package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesHistoriqueBO;

/**
 * @author qdeme
 *
 */
public interface DemandesHistoriqueRepository extends CrudRepository<DemandesHistoriqueBO, Integer> {

    public List<DemandesHistoriqueBO> findByFkDemandesPkDemandes(Integer pkDemandes);
    
}
