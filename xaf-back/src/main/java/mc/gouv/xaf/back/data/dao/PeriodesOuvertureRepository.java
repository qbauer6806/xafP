package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.PeriodesOuvertureBO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author qdeme
 */
public interface PeriodesOuvertureRepository extends CrudRepository<PeriodesOuvertureBO, Integer> {

    List<PeriodesOuvertureBO> findByDemarchePkDemarches(String demarcheId);

}
