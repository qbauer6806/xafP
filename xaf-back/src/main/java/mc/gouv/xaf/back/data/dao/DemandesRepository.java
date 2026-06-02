package mc.gouv.xaf.back.data.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import mc.gouv.xaf.back.data.projection.DemandePageableProjection;
import mc.gouv.xaf.back.data.projection.DemandeLightProjection;
import mc.gouv.xaf.back.data.projection.DemandeRecapProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author qdeme
 */
// On désactive la règle de Sonar sur le nommage des méthodes, car pour construire des requêtes on est obligé de mettre des '_'
@SuppressWarnings("java:S100")
public interface DemandesRepository extends JpaRepository<DemandeBO, Integer> {

    DemandeBO findByIdentifiant(String identifiant);

    DemandeBO findByPkDemandesAndUsagerId(Integer pkDemandes, Integer usagerId);

    /**
     * Récupération d'une demande pour une démarche et un usager AVEC un Accès actif
     */
    DemandeBO findByFkAccess_UsagerIdAndPkDemandesAndFkAccess_ActiveTrue(Integer usagerId, Integer id);

    /**
     * Permet de récupérer le nombre de demandes créées par un usager (courrier ou non)
     */
    Integer countByFkAccess_UsagerIdAndFkAccess_ActiveTrue(Integer usagerId);

    Page<DemandeBO> findAll(Pageable pageRequest);

    List<DemandeBO> findAllByIdentifiantIn(List<String> identifiants);

    List<DemandeBO> findAllByDernierStatut_Name(String dernierStatut);

    List<DemandeLightProjection> findAllByDernierStatut_NameIn(List<String> statuts);

    List<DemandeBO> findAllByDernierStatut_NameAndDernierStatutDateLessThan(String dernierStatut, Date date);

    List<DemandeBO> findAllByDernierStatut_NameAndDernierStatutDateGreaterThanAndDernierStatutDateLessThan(String dernierStatut, Date date1, Date date2);

    List<DemandeLightProjection> findByUsagerId(Integer usagerId);

    Optional<DemandeBO> findFirstByUsager_IdAndDernierStatut_NameInAndConfig_BuildIdInOrderByDateCreationDesc(
            Integer usagerId, List<String> statuts, List<String> buildIds);

    @Query("SELECT d.pkDemandes FROM DemandeBO d " + "JOIN d.dernierStatut ds "
            + "WHERE ds.date < :dernierStatutDateDebut " + "AND ds.name IN :dernierStatutList "
            + "AND d.canal IN :canaux")
    List<Integer> findPkDemandesByDernierStatutDateBeforeAndDernierStatutNameInAndCanalIn(
            @Param("dernierStatutDateDebut") Date dernierStatutDateDebut,
            @Param("dernierStatutList") List<String> dernierStatutList, @Param("canaux") List<String> canaux);

    /**
     * Permet de récupérer les demandes à purger dans un intervalle donné. Utile pour la relance par mail avant purge.
     * Permet de faire plusieurs relances par ex.
     *
     * @param dernierStatutDateDebut
     * @param dernierStatutDateFin
     * @param dernierStatutList
     * @return
     */
    List<DemandeBO> findByDernierStatut_DateBetweenAndDernierStatut_NameIn(Date dernierStatutDateDebut,
            Date dernierStatutDateFin, List<String> dernierStatutList);

    /**
     * Récupération demandes de l'usager FRONT (paginée)
     */
    Page<DemandeBO> findByFkAccessUsagerIdAndFkAccessActiveTrueAndDernierStatutNameIn(Integer usagerId,
            List<String> status, Pageable pageRequest);

    Page<DemandeBO> findByFkAccessUsagerIdAndFkAccessActiveTrue(Integer usagerId, Pageable pageable);

    @Query("""
            select d.pkDemandes as pkDemandes,
                   d.dateCreation as dateCreation,
                   d.identifiant as identifiant,
                   ds.pkDemandesStatuts as pkStatut,
                   ds.libelle as statutLibelle,
                   ds.name as statutName,
                   ds.date as statutDate
            from DemandeBO d
            left join d.dernierStatut ds
            where d.fkAccess.usagerId = :usagerId
              and d.fkAccess.active = true
            """)
    Page<DemandePageableProjection> findPageLightByFkAccessUsagerIdAndFkAccessActiveTrue(
            @Param("usagerId") Integer usagerId, Pageable pageable);

    @Query("""
            select d.pkDemandes as pkDemandes,
                   d.dateCreation as dateCreation,
                   d.identifiant as identifiant,
                   ds.pkDemandesStatuts as pkStatut,
                   ds.libelle as statutLibelle,
                   ds.name as statutName,
                   ds.date as statutDate
            from DemandeBO d
            left join d.dernierStatut ds
            where d.fkAccess.usagerId = :usagerId
              and d.fkAccess.active = true
              and ds.name in :status
            """)
    Page<DemandePageableProjection> findPageLightByFkAccessUsagerIdAndFkAccessActiveTrueAndDernierStatutNameIn(
            @Param("usagerId") Integer usagerId, @Param("status") List<String> status, Pageable pageable);

    List<DemandeRecapProjection> findByFkAccessUsagerIdAndFkAccessActiveTrue(Integer usagerId);

    boolean existsByAgent(DemandesAgentsBO agent);

    boolean existsByUsager(DemandesUsagersBO usager);

    Optional<DemandeBO> findFirstByOrderByDateCreationDesc();


}

