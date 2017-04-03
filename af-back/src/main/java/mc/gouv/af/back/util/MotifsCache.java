package mc.gouv.af.back.util;

import java.util.List;

import mc.gouv.dem.apishared.model.DemandeStatutEnum;
import mc.gouv.dem.apishared.model.MotifDTO;

/**
 * Composant permettant de gérer un cache des motifs de la démarche courante
 * 
 * @author qdeme
 * 
 */
public interface MotifsCache {

    /**
     * Force le refresh de la liste des motifs depuis le WS puis retourne la liste
     * 
     * @return
     */
    public List<MotifDTO> fetchMotifs();

    // /**
    // * Permet de retourner le motif correspondant à un certain codeMotif, un certain statut, et dans
    // * la langue souhaitée
    // * @param codeMotif
    // * @param statut
    // * @param langue
    // * @return
    // */
    // public MotifDTO getMotif(String codeMotif, DemandeStatutEnum statut, String langue);

    /**
     * Permet de retourner le motif correspondant à un certain codeMotif et dans la langue souhaitée
     * 
     * @param codeMotif
     * @param langue
     * @return
     */
    public MotifDTO getMotif(String codeMotif, String langue);

    /**
     * Permet de retourner tous les motifs d'une certaine langue
     * 
     * @param langue
     * @return
     */
    public List<MotifDTO> getMotifs(String langue);

    /**
     * Permet de retourner tous les motifs d'une certaine langue et d'un certain statut
     * 
     * @param langue
     * @param statut
     * @return
     */
    public List<MotifDTO> getMotifs(String langue, DemandeStatutEnum statut);

    /**
     * Permet de récupérer la liste "cachée" de motifs actifs de la démarche courante
     * 
     * @return
     */
    public List<MotifDTO> getActiveMotifs();

    /**
     * Permet de récupérer la liste "cachée" de motifs relatifs à la langue et au statut de la demande
     * 
     * @return
     */
    public List<MotifDTO> getActiveMotifs(String langue, DemandeStatutEnum statut);

}
