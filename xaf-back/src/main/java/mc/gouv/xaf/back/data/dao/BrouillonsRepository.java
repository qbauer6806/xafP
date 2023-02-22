package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import mc.gouv.xaf.back.data.entity.BrouillonBO;

/**
 * @author qdeme
 */
public interface BrouillonsRepository extends CrudRepository<BrouillonBO, Integer> {

    /**
     * Récupération d'un brouillon pour une démarche et un usager
     */
    BrouillonBO findByFkAccessDemarcheIdAndPkBrouillons(String demarcheId, Integer pkBrouillons);

    /**
     * Récupération brouillons de l'usager FRONT (paginée)
     */
    @Query("select b from BrouillonBO b inner join b.fkAccess fa " +
     "where fa.usagerId = :usagerId and fa.demarcheId = :demarcheId and fa.active = :active")
    Page<BrouillonBO> findByDemarcheIdAndIdAndUsagerIdAndActive(@Param("demarcheId") String demarcheId, @Param("usagerId") Integer usagerId, boolean active,
                                                               Pageable pageRequest);
    
    /**
     * Récupération brouillons de l'usager FRONT
     */
    @Query("select b from BrouillonBO b inner join b.fkAccess fa " +
     "where fa.usagerId = :usagerId and fa.demarcheId = :demarcheId")
    List<BrouillonBO> findByDemarcheIdAndUsagerId(@Param("demarcheId") String demarcheId, @Param("usagerId") Integer usagerId);
}
