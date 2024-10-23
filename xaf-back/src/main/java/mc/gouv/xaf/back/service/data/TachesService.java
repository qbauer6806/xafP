package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.TacheDTO;

import java.util.List;

/**
 * @author mboutelier.ext
 */
public interface TachesService {

    /**
     * Récupère la tâche demandée en fonction de sa PK
     *
     * @param pkTaches,
     *         l'id de la tâche
     * @return la tâche associée à l'ID donné
     */
    TacheDTO getTacheByID(Integer pkTaches);

    /**
     * Récupère toutes les tâches pour une demande donnée.
     *
     * @param demandeId,
     *         ID de la demande
     * @return une liste de tâches
     */
    List<TacheDTO> getTachesByDemandeID(Integer demandeId);

    /**
     * Ajoute ou mets à jour une Taches
     *
     * @param toSave
     *         La tâche à sauvegarder
     * @return la Taches sauvée
     */
    TacheDTO saveOrUpdate(TacheDTO toSave);

    /**
     * Créer une liste de tâches lors de la création de la demande.
     * <br>
     * A implémenter dans les démarches en fonction des règles métiers.
     *
     * @param demande,
     *         la demande
     * @return une liste de tâches
     */
    List<TacheDTO> creerListeDeTaches(DemandeDTO demande);

    /**
     * Met à jour les statuts des tâches pour les demandes en retour guichet :
     * <ul>
     * <li>Supprime les statuts sélectionnés RETOUR_GUICHET.</li>
     * <li>Recopie la décision de l'agent valideur.</li>
     * <li>Bloque l'édition des tâches validées et refusées</li>
     * </ul>
     *
     * @param pkDemandes,
     *         l'id de la demande en retour Guichet
     */
    void updateTachesPourRetourGuichet(Integer pkDemandes);

    /**
     * Bloque l'édition des tâches validées et refusées
     *
     * @param pkDemandes,
     *         l'id de la demande en retour Guichet
     */
    void updateTachesLock(Integer pkDemandes);

    /**
     * Purge des tâches liées à une demande.
     *
     * @param pkDemande,
     *         l'id de la demande
     */
    void deleteTaches(Integer pkDemande);

}
