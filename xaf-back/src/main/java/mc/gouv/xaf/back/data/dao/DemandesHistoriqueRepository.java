package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandesHistoriqueBO;

/**
 * @author qdeme
 *
 */
public interface DemandesHistoriqueRepository extends CrudRepository<DemandesHistoriqueBO, Integer> {

    public List<DemandesHistoriqueBO> findByFkDemandesPkDemandes(Integer pkDemandes);
    
    @Modifying
    @Query("delete from DemandesHistoriqueBO histo where histo.fkDemandes.pkDemandes =:pkDemandes ")
    public void deleteHistoForGivenPkDemandes(Integer pkDemandes);
}
