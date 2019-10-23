package mc.gouv.xaf.back.data.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import mc.gouv.xaf.back.data.entity.DemandeBO;

import java.util.Date;
import java.util.List;

/**
 * @author qdeme
 *
 */
public interface DemandesRepository extends CrudRepository<DemandeBO, Integer> {

    public DemandeBO findByIdentifiant(String identifiant);

    /**
     * Récupération d'une demande pour une démarche et un usager AVEC un Accès actif
     * 
     * @param demarcheId
     * @param id
     * @param usagerId
     * @return
     */
    @Query("select d from DemandeBO d inner join d.fkAccess fa where fa.usagerId = :usagerId and fa.demarcheId= :demarcheId and d.pkDemandes = :id and fa.active = true")
    public DemandeBO findByDemarcheIdAndIdAndUsagerId(@Param("demarcheId") String demarcheId, @Param("id") Integer id,
            @Param("usagerId") Integer usagerId);

    /**
     * Permet de récupérer le nombre de demandes créées par un usager (courrier ou non)
     * 
     * @param demarcheId
     * @param usagerId
     * @return
     */
    @Query("select count(d) from DemandeBO d inner join d.fkAccess fa where fa.usagerId = :usagerId and fa.demarcheId= :demarcheId")
    public Integer getNbDemandesForUsager(@Param("demarcheId") String demarcheId, @Param("usagerId") Integer usagerId);

    Page<DemandeBO> findAll(Pageable pageRequest);

    /**
     * Permet de récupérer les demandes créées entre deux dates
     * @param demarcheId
     * @param startDate
     * @param endDate
     * @return
     */
    @Query("select d from DemandeBO d inner join d.fkAccess fa where fa.demarcheId = :demarcheId and d.dateCreation between :startDate and :endDate")
    List<DemandeBO> findAllByDemarcheIdAndDateCreationBetween(String demarcheId, Date startDate, Date endDate);

    /**
     * Permet de récupérer les demandes créées à partir d'une date donnée
     * @param demarcheId
     * @param startDate
     * @return
     */
    @Query("select d from DemandeBO d inner join d.fkAccess fa where fa.demarcheId = :demarcheId and d.dateCreation >= :startDate")
    List<DemandeBO> findAllByDemarcheIdAndDateCreationFrom(String demarcheId, Date startDate);

    /**
     * Permet de récupérer les demandes créées à jusqu'à une date donnée
     * @param demarcheId
     * @param endDate
     * @return
     */
    @Query("select d from DemandeBO d inner join d.fkAccess fa where fa.demarcheId = :demarcheId and d.dateCreation <= :endDate")
    List<DemandeBO> findAllByDemarcheIdAndDateCreationUntil(String demarcheId, Date endDate);
}
