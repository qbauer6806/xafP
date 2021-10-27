package mc.gouv.xaf.back.data.dao;

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
     *
     * @param demarcheId
     * @param id
     * @param usagerId
     * @return
     */
    BrouillonBO findByFkAccessDemarcheIdAndPkBrouillons(String demarcheId, Integer pkBrouillons);

    /**
     * Récupération brouillons de l'usager FRONT (paginée)
     */
    @Query("select b from BrouillonBO b inner join b.fkAccess fa " +
            "where fa.usagerId = :usagerId and fa.demarcheId = :demarcheId")
    Page<BrouillonBO> findByDemarcheIdAndIdAndUsagerId(@Param("demarcheId") String demarcheId, @Param("usagerId") Integer usagerId,
                                                               Pageable pageRequest);
}
