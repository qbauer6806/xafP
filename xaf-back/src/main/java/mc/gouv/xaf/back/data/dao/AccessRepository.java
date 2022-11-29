package mc.gouv.xaf.back.data.dao;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.AccessBO;

import java.util.List;

/**
 * 
 * @author qdeme
 *
 */
public interface AccessRepository extends CrudRepository<AccessBO, Integer> {

    /**
     * Retourne les accès correspondant à ce demarcheId et cet usagerId
     * Normalement il n'y en a un seul en base qui correspond à ce couple (demarcheId,usagerId) et qui ait Active = true
     * Sinon cela signifie que la DB est dans un état incohérent
     */
    List<AccessBO> getByDemarcheIdAndUsagerIdAndActive(String demarcheId, Integer usagerId, boolean active);
    
    List<AccessBO> getByDemarcheIdAndActive(String demarcheId, boolean active);
    
    List<AccessBO> getByDemarcheId(String demarcheId);
    
}
