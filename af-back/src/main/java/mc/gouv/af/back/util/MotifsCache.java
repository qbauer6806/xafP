package mc.gouv.af.back.util;

import java.util.List;

import mc.gouv.dem.apishared.model.MotifDTO;

/**
 * Composant permettant de gérer un cache des motifs de la démarche courante
 * 
 * @author qdeme
 *
 */
public interface MotifsCache {

    /**
     * Permet de récupérer la liste "cachée" de motifs de la démarche courante
     * @return
     */
    public List<MotifDTO> getMotifs();
    
    /**
     * Force le refresh de la liste des motifs depuis le WS puis retourne la liste
     * @return
     */
    public List<MotifDTO> fetchMotifs();
    
//    /**
//     * Permet de retourner le motif correspondant à un certain codeMotif, un certain statut, et dans
//     * la langue souhaitée
//     * @param codeMotif
//     * @param statut
//     * @param langue
//     * @return
//     */
//    public MotifDTO getMotif(String codeMotif, DemandeStatutEnum statut, String langue);
    
    /**
     * Permet de retourner le motif correspondant à un certain codeMotif et dans la langue souhaitée
     * 
     * @param codeMotif
     * @param langue
     * @return
     */
    public MotifDTO getMotif(String codeMotif, String langue);
    
}
