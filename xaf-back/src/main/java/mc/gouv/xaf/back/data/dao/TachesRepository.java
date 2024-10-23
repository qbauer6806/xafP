package mc.gouv.xaf.back.data.dao;

import mc.gouv.xaf.back.data.entity.TacheBO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author mboutelier.ext
 */
// On désactive la règle de Sonar sur le nommage des méthodes, car pour construire des requêtes on est obligé de mettre des '_'
@SuppressWarnings("java:S100")
public interface TachesRepository extends CrudRepository<TacheBO, Integer> {

    /**
     * Méthode pour récupérer les tâches liées à une demande.
     * <br>
     * On tri sur le flag locked pour avoir toutes les tâches validées par un agent valideur à la fin.
     * <br>
     * On tri par pkTaches pour éviter que la liste ne change d'ordre après une modification.
     *
     * @param pkDemandes,
     *         l'id de la demande
     * @return une liste de tâches liées à la demande demandée
     */
    @Query("SELECT t FROM TacheBO t WHERE t.demande.pkDemandes = ?1 ORDER BY t.locked, t.pkTaches")
    List<TacheBO> findByPkDemandes(Integer pkDemandes);

    /**
     * Méthode pour bloquer les tâches liées à une demande
     *
     * @param pkDemandes,
     *         l'id de la demande
     */
    @Modifying
    @Query("UPDATE TacheBO t SET t.locked = true WHERE t.demande.pkDemandes = ?1")
    void updateTachesLock(Integer pkDemandes);

    /**
     * Purge des tâches liées à une demande
     *
     * @param pkDemande,
     *         l'id de la demande
     */
    void deleteByDemande_PkDemandes(Integer pkDemande);
}
