package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.PeriodesOuvertureBO;

/**
 * 
 * @author qdeme
 *
 */
public interface PeriodesOuvertureRepository extends CrudRepository<PeriodesOuvertureBO, Integer> {

    public List<PeriodesOuvertureBO> findByDemarchePkDemarches(String demarcheId);
    
}
