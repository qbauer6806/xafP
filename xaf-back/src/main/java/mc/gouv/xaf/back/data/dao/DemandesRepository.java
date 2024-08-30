package mc.gouv.xaf.back.data.dao;

import java.util.Date;
import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import mc.gouv.xaf.shared.dto.DemandeRecapProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author qdeme
 */
// On désactive la règle de Sonar sur le nommage des méthodes, car pour construire des requêtes on est obligé de mettre des '_'
@SuppressWarnings("java:S100")
public interface DemandesRepository extends CrudRepository<DemandeBO, Integer> {

    DemandeBO findByIdentifiant(String identifiant);

    /**
     * Récupération d'une demande pour une démarche et un usager AVEC un Accès actif
     */
    @Query("select d from DemandeBO d inner join d.fkAccess fa where fa.usagerId = :usagerId and fa.demarcheId= :demarcheId and d.pkDemandes = :id and fa.active = true")
    DemandeBO findByDemarcheIdAndIdAndUsagerId(@Param("demarcheId") String demarcheId, @Param("id") Integer id,
            @Param("usagerId") Integer usagerId);

    /**
     * Permet de récupérer le nombre de demandes créées par un usager (courrier ou non)
     */
    Integer countByFkAccess_UsagerIdAndFkAccess_ActiveTrue(Integer usagerId);

    Page<DemandeBO> findAll(Pageable pageRequest);

    List<DemandeBO> findAll();

    List<DemandeBO> findAllByIdentifiantIn(List<String> identifiants);

    List<DemandeBO> findAllByDernierStatut_Name(String dernierStatut);

    List<DemandeBO> findAllByDernierStatut_NameAndDernierStatutDateLessThan(String dernierStatut, Date date);

    /**
     * Permet de récupérer les demandes créées entre deux dates
     */
    @Query("select d from DemandeBO d where d.dateCreation between :startDate and :endDate")
    List<DemandeBO> findAllByDateCreationBetween(Date startDate, Date endDate);

    /**
     * Permet de récupérer les demandes créées à partir d'une date donnée
     */
    @Query("select d from DemandeBO d where d.dateCreation >= :startDate")
    List<DemandeBO> findAllByDateCreationFrom(Date startDate);

    /**
     * Permet de récupérer les demandes créées à jusqu'à une date donnée
     */
    @Query("select d from DemandeBO d where d.dateCreation <= :endDate")
    List<DemandeBO> findAllByDateCreationUntil(Date endDate);

    /**
     * Permet de récupérer les demandes créées entre deux dates
     */
    @Query("select d from DemandeBO d inner join d.dernierStatut ds where d.dateCreation between :startDate and :endDate and ds.name = :dernierStatut")
    List<DemandeBO> findAllByDateCreationBetweenAndDernierStatut(Date startDate, Date endDate, String dernierStatut);

    /**
     * Permet de récupérer les demandes créées à partir d'une date donnée
     */
    @Query("select d from DemandeBO d inner join d.dernierStatut ds where d.dateCreation >= :startDate and ds.name = :dernierStatut")
    List<DemandeBO> findAllByDateCreationFromAndDernierStatut(Date startDate, String dernierStatut);


    /**
     * Permet de récupérer les demandes à purger avant une certaine date. Utile pour l'opération de purge
     *
     * @param dernierStatutDateDebut
     * @param dernierStatutList
     * @param canaux
     * @return
     */
    @Query("select d from DemandeBO d inner join d.dernierStatut ds inner join d.fkAccess access where ds.date < :dernierStatutDateDebut and ds.name in :dernierStatutList and d.canal in :canaux")
    List<DemandeBO> findAllWithDateDernierStatutBeforeAndNameStatutIn(Date dernierStatutDateDebut,
            List<String> dernierStatutList, List<String> canaux);

    @Query("select d.pkDemandes from DemandeBO d inner join d.dernierStatut ds inner join d.fkAccess access where ds.date < :dernierStatutDateDebut and ds.name in :dernierStatutList and d.canal in :canaux")
    List<Integer> findAllIdsWithDateDernierStatutBeforeAndNameStatutIn(Date dernierStatutDateDebut,
            List<String> dernierStatutList, List<String> canaux);

    /**
     * Permet de récupérer les demandes à purger dans un intervalle donné. Utile pour la relance par mail avant purge.
     * Permet de faire plusieurs relances par ex.
     * 
     * @param dernierStatutDateDebut
     * @param dernierStatutDateFin
     * @param dernierStatutList
     * @return
     */
    @Query("select d from DemandeBO d inner join d.dernierStatut ds inner join d.fkAccess access where ds.date >= :dernierStatutDateDebut and ds.date < :dernierStatutDateFin and ds.name in :dernierStatutList")
    List<DemandeBO> findAllWithDateDernierStatutBetweenAndNameStatutIn(Date dernierStatutDateDebut,
            Date dernierStatutDateFin,
            List<String> dernierStatutList);

    @Query("select d.pkDemandes from DemandeBO d inner join d.dernierStatut ds inner join d.fkAccess access where ds.date >= :dernierStatutDateDebut and ds.date < :dernierStatutDateFin and ds.name in :dernierStatutList")
    List<Integer> findAllIdsWithDateDernierStatutBetweenAndNameStatutIn(Date dernierStatutDateDebut,
            Date dernierStatutDateFin, List<String> dernierStatutList);

    /**
     * Permet de récupérer les demandes créées par les canaux courrier et guichet physique
     */
    @Query("select d from DemandeBO d where d.canal='COURRIER' or d.canal='GUICHET_PHYSIQUE'")
    List<DemandeBO> findAllDemandesCourrier();

    /**
     * Permet de récupérer les demandes créées à jusqu'à une date donnée
     */
    @Query("select d from DemandeBO d inner join d.dernierStatut ds where d.dateCreation <= :endDate and ds.name = :dernierStatut")
    List<DemandeBO> findAllByDateCreationUntilAndDernierStatut(Date endDate, String dernierStatut);

    /**
     * Récupération demandes de l'usager FRONT (paginée)
     */
    @Query("select d from DemandeBO d inner join d.fkAccess fa inner join TraductionBO t on (d.dernierStatut.name = t.cle and t.langue = :langue) "
            + "where fa.usagerId = :usagerId and fa.demarcheId = :demarcheId and fa.active = true and d.dernierStatut.name in :status")
    Page<DemandeBO> findByDemarcheIdAndIdAndUsagerIdAndStatuts(@Param("demarcheId") String demarcheId,
            @Param("usagerId") Integer usagerId, @Param("status") String[] status, @Param("langue") String langue,
            Pageable pageRequest);

    @Query("select d.pkDemandes as pkDemandes, d.identifiant as identifiant, d.dateCreation as dateCreation, s.name as dernierStatut from DemandeBO d inner join d.fkAccess fa inner join d.dernierStatut s where fa.usagerId = :usagerId and fa.demarcheId= :demarcheId and fa.active = true and s.fkDemandes.pkDemandes = d.pkDemandes")
    List<DemandeRecapProjection> findByUsagerIdForDemandeRecapDTO(@Param("demarcheId") String demarcheId,
            @Param("usagerId") Integer usagerId);

    /**
     * Récupération des demandes d'usager FRONT
     */
    @Query("select d from DemandeBO d inner join d.fkAccess fa where fa.demarcheId = :demarcheId and fa.usagerId = :usagerId")
    List<DemandeBO> findByDemarcheIdAndUsagerId(@Param("demarcheId") String demarcheId,
            @Param("usagerId") Integer usagerId);

    @Query("SELECT COUNT(d) > 0 FROM DemandeBO d WHERE d.agent = :agent")
    boolean existsByAgent(@Param("agent") DemandesAgentsBO agent);

    @Query("SELECT COUNT(d) > 0 FROM DemandeBO d WHERE d.usager = :usager")
    boolean existsByUsager(@Param("usager") DemandesUsagersBO usager);
}
