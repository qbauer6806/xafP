package mc.gouv.xaf.back.service.data;

import java.util.List;
import java.util.Map;

import mc.gouv.xaf.shared.dto.MotifDTO;

/**
 * Service permettant la manipulation des motifs.
 * 
 * @author qdeme
 *
 */
public interface MotifsService {
    
    /**
     * Permet de récupérer le motif correspondant à un DemarcheID et un MotifID
     * @return Le motif demandé
     */
    MotifDTO getMotif(String demarcheId, Integer pkMotif);

    /**
     * <p>Permet de récupérer pour un statut donné une map contenant les motifs.</p>
     * <p>La clé de la map est un composé du code du motif et de la langue (ex: CODE_MOTIF_FR).</p>
     * @return une map contenant une chaine et un motif dto
     */
    Map<String, MotifDTO> getMotifs(String demarcheId, String statut);
    
    /**
     * Permet de récupérer les motifs correspondant à un DemarcheID
     * @return Les motifs demandés
     */
    List<MotifDTO> getMotifs(String demarcheId);

    /**
     * Permet de sauvegarder ou mettre à jour un motif en base
     * @param motif, DTO à sauvegarder
     * @return Le motif sauvegardé ou mis à jour
     */
    MotifDTO saveOrUpdateMotif(String demarcheId, MotifDTO motif);
    
    /**
     * Permet de supprimer un motif à partir du DemarcheID et du MotifID
     * @param demarcheId, id de la démarche
     * @param pkMotif, id du motif
     */
    void deleteMotif(String demarcheId, Integer pkMotif);

}
