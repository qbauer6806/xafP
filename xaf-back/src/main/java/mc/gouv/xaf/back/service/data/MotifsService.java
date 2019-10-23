package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.back.shared.dto.MotifDTO;

/**
 * Service permettant la manipulation des motifs.
 * 
 * @author qdeme
 *
 */
public interface MotifsService {
    
    /**
     * Permet de récupérer le motif correspondant à un DemarcheID et un MotifID
     * @param motif
     * @return Le motif demandé
     */
    public MotifDTO getMotif(String demarcheId, Integer pkMotif);
    
    /**
     * Permet de récupérer les motifs correspondant à un DemarcheID
     * @param motif
     * @return Les motifs demandés
     */
    public List<MotifDTO> getMotifs(String demarcheId);

    /**
     * Permet de sauvegarder ou mettre à jour un motif en base
     * @param motif
     * @return Le motif sauvegardé ou mis à jour
     */
    public MotifDTO saveOrUpdateMotif(String demarcheId, MotifDTO motif);
    
    /**
     * Permet de supprimer un motif à partir du DemarcheID et du MotifID
     * @param motif
     */
    public void deleteMotif(String demarcheId, Integer pkMotif);

}
