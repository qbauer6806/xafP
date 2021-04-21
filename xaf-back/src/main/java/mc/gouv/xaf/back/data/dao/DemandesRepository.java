package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * @author qdeme
 */
public interface DemandesRepository extends CrudRepository<DemandeBO, Integer> {

    DemandeBO findByIdentifiant(String identifiant);

    /**
     * Récupération d'une demande pour une démarche et un usager AVEC un Accès actif
     *
     * @param demarcheId
     * @param id
     * @param usagerId
     * @return
     */
    @Query("select d from DemandeBO d inner join d.fkAccess fa where fa.usagerId = :usagerId and fa.demarcheId= :demarcheId and d.pkDemandes = :id and fa.active = true")
    DemandeBO findByDemarcheIdAndIdAndUsagerId(@Param("demarcheId") String demarcheId, @Param("id") Integer id,
                                               @Param("usagerId") Integer usagerId);

    /**
     * Permet de récupérer le nombre de demandes créées par un usager (courrier ou non)
     *
     * @param demarcheId
     * @param usagerId
     * @return
     */
    @Query("select count(d) from DemandeBO d inner join d.fkAccess fa where fa.usagerId = :usagerId and fa.demarcheId= :demarcheId and fa.active = true")
    Integer getNbDemandesForUsager(@Param("demarcheId") String demarcheId, @Param("usagerId") Integer usagerId);

    Page<DemandeBO> findAll(Pageable pageRequest);

    List<DemandeBO> findAll();

    List<DemandeBO> findAllByIdentifiantIn(List<String> identifiants);

    List<DemandeBO> findAllByDernierStatut_Libelle(String dernierStatut);

    /**
     * Permet de récupérer les demandes créées entre deux dates
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Query("select d from DemandeBO d where d.dateCreation between :startDate and :endDate")
    List<DemandeBO> findAllByDateCreationBetween(Date startDate, Date endDate);

    /**
     * Permet de récupérer les demandes créées à partir d'une date donnée
     *
     * @param startDate
     * @return
     */
    @Query("select d from DemandeBO d where d.dateCreation >= :startDate")
    List<DemandeBO> findAllByDateCreationFrom(Date startDate);

    /**
     * Permet de récupérer les demandes créées à jusqu'à une date donnée
     *
     * @param endDate
     * @return
     */
    @Query("select d from DemandeBO d where d.dateCreation <= :endDate")
    List<DemandeBO> findAllByDateCreationUntil(Date endDate);

    /**
     * Permet de récupérer les demandes créées entre deux dates
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Query("select d from DemandeBO d inner join d.dernierStatut ds where d.dateCreation between :startDate and :endDate and ds.libelle = :dernierStatut")
    List<DemandeBO> findAllByDateCreationBetweenAndDernierStatut(Date startDate, Date endDate, String dernierStatut);

    /**
     * Permet de récupérer les demandes créées à partir d'une date donnée
     *
     * @param startDate
     * @return
     */
    @Query("select d from DemandeBO d inner join d.dernierStatut ds where d.dateCreation >= :startDate and ds.libelle = :dernierStatut")
    List<DemandeBO> findAllByDateCreationFromAndDernierStatut(Date startDate, String dernierStatut);

    /**
     * Permet de récupérer les demandes créées à jusqu'à une date donnée
     *
     * @param endDate
     * @return
     */
    @Query("select d from DemandeBO d inner join d.dernierStatut ds where d.dateCreation <= :endDate and ds.libelle = :dernierStatut")
    List<DemandeBO> findAllByDateCreationUntilAndDernierStatut(Date endDate, String dernierStatut);

    /**
     * Récupération demandes de l'usager FRONT (paginée)
     */
    @Query("select d from DemandeBO d inner join d.fkAccess fa inner join TraductionBO t on (d.dernierStatut.libelle = t.cle and t.langue = :langue) " +
            "where fa.usagerId = :usagerId and fa.demarcheId = :demarcheId and fa.active = true and d.dernierStatut.libelle in :status")
    Page<DemandeBO> findByDemarcheIdAndIdAndUsagerIdAndStatuts(@Param("demarcheId") String demarcheId, @Param("usagerId") Integer usagerId,
                                                               @Param("status") String[] status, @Param("langue") String langue,
                                                               Pageable pageRequest);
}
