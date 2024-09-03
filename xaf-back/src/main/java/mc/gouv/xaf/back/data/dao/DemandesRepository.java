package mc.gouv.xaf.back.data.dao;

import java.util.Date;
import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
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
    DemandeBO findByFkAccess_UsagerIdAndPkDemandesAndFkAccess_ActiveTrue(Integer usagerId, Integer id);

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
    List<DemandeBO> findByDateCreationBetween(Date startDate, Date endDate);

    /**
     * Permet de récupérer les demandes créées à partir d'une date donnée
     */
    List<DemandeBO> findByDateCreationGreaterThanEqual(Date startDate);

    /**
     * Permet de récupérer les demandes créées à jusqu'à une date donnée
     */
    List<DemandeBO> findByDateCreationLessThanEqual(Date endDate);

    /**
     * Permet de récupérer les demandes créées entre deux dates
     */
    List<DemandeBO> findByDateCreationBetweenAndDernierStatut_Name(Date startDate, Date endDate, String dernierStatut);

    /**
     * Permet de récupérer les demandes créées à partir d'une date donnée
     */
    List<DemandeBO> findByDateCreationGreaterThanEqualAndDernierStatut_Name(Date startDate, String dernierStatut);

    /**
     * Permet de récupérer les demandes à purger avant une certaine date. Utile pour l'opération de purge
     *
     * @param dernierStatutDateDebut
     * @param dernierStatutList
     * @param canaux
     * @return
     */
    List<DemandeBO> findByDernierStatut_DateBeforeAndDernierStatut_NameInAndCanalIn(Date dernierStatutDateDebut, List<String> dernierStatutList, List<String> canaux);

    List<Integer> findPkDemandesByDernierStatut_DateBeforeAndDernierStatut_NameInAndCanalIn(Date dernierStatutDateDebut, List<String> dernierStatutList, List<String> canaux);

    /**
     * Permet de récupérer les demandes à purger dans un intervalle donné. Utile pour la relance par mail avant purge.
     * Permet de faire plusieurs relances par ex.
     * 
     * @param dernierStatutDateDebut
     * @param dernierStatutDateFin
     * @param dernierStatutList
     * @return
     */
    List<DemandeBO> findByDernierStatut_DateBetweenAndDernierStatut_NameIn(Date dernierStatutDateDebut, Date dernierStatutDateFin, List<String> dernierStatutList);

    List<Integer> findPkDemandesByDernierStatut_DateBetweenAndDernierStatut_NameIn(Date dernierStatutDateDebut, Date dernierStatutDateFin, List<String> dernierStatutList);

    /**
     * Permet de récupérer les demandes créées à jusqu'à une date donnée
     */
    List<DemandeBO> findByDateCreationLessThanEqualAndDernierStatut_Name(Date endDate, String dernierStatut);

    /**
     * Récupération demandes de l'usager FRONT (paginée)
     */
    @Query("select d from DemandeBO d inner join d.fkAccess fa inner join TraductionBO t on (d.dernierStatut.name = t.cle and t.langue = :langue) "
            + "where fa.usagerId = :usagerId and fa.active = true and d.dernierStatut.name in :status")
    Page<DemandeBO> findByUsagerIdAndStatuts(@Param("usagerId") Integer usagerId, @Param("status") String[] status, @Param("langue") String langue,
            Pageable pageRequest);

    @Query("select d.pkDemandes as pkDemandes, d.identifiant as identifiant, d.dateCreation as dateCreation, s.name as dernierStatut from DemandeBO d inner join d.fkAccess fa inner join d.dernierStatut s where fa.usagerId = :usagerId and fa.active = true and s.fkDemandes.pkDemandes = d.pkDemandes")
    List<DemandeRecapProjection> findByUsagerIdForDemandeRecapDTO(@Param("usagerId") Integer usagerId);

    boolean existsByAgent(DemandesAgentsBO agent);

    boolean existsByUsager(DemandesAgentsBO agent);
}
