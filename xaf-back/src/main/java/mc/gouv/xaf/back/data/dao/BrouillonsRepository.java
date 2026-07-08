package mc.gouv.xaf.back.data.dao;

import java.util.Date;
import java.util.List;
import mc.gouv.xaf.back.data.entity.BrouillonBO;
import mc.gouv.xaf.back.data.projection.BrouillonPageableProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select b.pkBrouillons as pkBrouillons,
                   b.dateCreation as dateCreation,
                   b.dateDerModif as dateDerModif,
                   c.buildId as buildId,
                   b.recapType as recapType
            from BrouillonBO b
            left join b.config c
            where b.fkAccess.usagerId = :usagerId
              and b.fkAccess.active = true
            """)
    Page<BrouillonPageableProjection> findPageLightByFkAccess_UsagerIdAndFkAccess_Active(
            @Param("usagerId") Integer usagerId, Pageable pageRequest);

    /**
     * Récupération brouillons de l'usager FRONT
     */
    List<BrouillonBO> findByFkAccess_UsagerId(Integer usagerId);

    @Modifying
    @Query("delete from BrouillonBO b where b.config.buildId != :buildIdCourant")
    void deleteBrouillonsWithBuildIdOtherThan(String buildIdCourant);

    @Query("select count(BBO) from BrouillonBO BBO where BBO.config.buildId != :buildIdCourant")
    Long getCountBrouillonsWithBuildIdOtherThan(String buildIdCourant);

    @Modifying
    @Query("UPDATE BrouillonBO b SET b.config.buildId = :newBuildId WHERE b.config.buildId = :oldBuildId")
    void updateBuildIdForBrouillons(String oldBuildId, String newBuildId);

    @Query("""
            select b
            from BrouillonBO b
            join b.fkAccess a
            where b.dateCreation is not null
            and a.usagerId is not null
            and b.dateCreation < :dateFinSuppression
            """)
    Page<BrouillonBO> findBrouillonsASupprimer(Date dateFinSuppression, Pageable pageable);

    @Query("""
            select b
            from BrouillonBO b
            join b.fkAccess a
            where b.dateCreation is not null
            and a.usagerId is not null
            and b.dateCreation >= :debut
            and b.dateCreation < :fin
            """)
    Page<BrouillonBO> findBrouillonsParDateCreation(
            Date debut,
            Date fin,
            Pageable pageable);
}
