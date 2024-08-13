package mc.gouv.xaf.back.data.dao;

import java.util.stream.Stream;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * 
 * @author qdeme
 *
 */
public interface DemandesStatutsRepository extends CrudRepository<DemandesStatutsBO, Integer> {
	DemandesStatutsBO findByFkDemandesPkDemandesAndName(Integer fkDemandes, String name);

    @Query("SELECT d FROM DemandesStatutsBO d")
    Stream<DemandesStatutsBO> streamAll();
}
