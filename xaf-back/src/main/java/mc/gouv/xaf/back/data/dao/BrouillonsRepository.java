package mc.gouv.xaf.back.data.dao;

import java.util.List;
import mc.gouv.xaf.back.data.entity.BrouillonBO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * @author qdeme
 */
public interface BrouillonsRepository extends CrudRepository<BrouillonBO, Integer> {

    /**
     * Récupération d'un brouillon pour une démarche et un usager
     */
    BrouillonBO findByPkBrouillons(Integer pkBrouillons);

    /**
     * Récupération brouillons de l'usager FRONT (paginée)
     */
    Page<BrouillonBO> findByFkAccess_UsagerIdAndFkAccess_Active(Integer usagerId, boolean active, Pageable pageRequest);
    
    /**
     * Récupération brouillons de l'usager FRONT
     */
    List<BrouillonBO> findByFkAccess_UsagerId(Integer usagerId);
}
