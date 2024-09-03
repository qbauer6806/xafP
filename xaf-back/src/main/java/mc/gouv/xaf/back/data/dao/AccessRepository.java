package mc.gouv.xaf.back.data.dao;

import java.util.List;
import java.util.Optional;
import mc.gouv.xaf.back.data.entity.AccessBO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

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
    Optional<AccessBO> findFirstByUsagerIdAndActive(Integer usagerId, boolean active);
    
    List<AccessBO> findByActive(boolean active);

    List<AccessBO> findAll();

    @Query("SELECT DISTINCT a.usagerId FROM AccessBO a")
    List<Integer> findDistinctUsagerId();
    
}
