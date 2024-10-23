package mc.gouv.xaf.back.service.data;

import java.util.List;
import java.util.Map;

import mc.gouv.xaf.shared.dto.MotifDTO;

/**
 * Service permettant la manipulation des motifs.
 *
 * @author qdeme
 */
public interface MotifsService {

    /**
     * Permet de récupérer le motif correspondant à un MotifID
     *
     * @return Le motif demandé
     */
    MotifDTO getMotif(Integer pkMotif);

    /**
     * <p>Permet de récupérer pour un statut donné une map contenant les motifs.</p>
     * <p>La clé de la map est un composé du code du motif et de la langue (ex: CODE_MOTIF_FR).</p>
     *
     * @return une map contenant une chaine et un motif dto
     */
    Map<String, MotifDTO> getMotifsByStatut(String statut);

    /**
     * Permet de récupérer les motifs correspondant
     *
     * @return Les motifs demandés
     */
    List<MotifDTO> getMotifs();

    /**
     * Permet de sauvegarder ou mettre à jour un motif en base
     *
     * @param motif,
     *         DTO à sauvegarder
     * @return Le motif sauvegardé ou mis à jour
     */
    MotifDTO saveOrUpdateMotif(MotifDTO motif);

    /**
     * <p>Permet de supprimer un motif à partir du MotifID</p>
     * <p>Attention : ce n'est pas une suppression réelle, mais plutôt un archivage.</p>
     *
     * @param pkMotif,
     *         id du motif
     */
    void deleteMotif(Integer pkMotif);

}
