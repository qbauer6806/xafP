package mc.gouv.af.back.util;

import java.util.List;

import mc.gouv.dem.shared.model.DemandeStatutEnum;
import mc.gouv.dem.shared.model.MotifDTO;
import mc.gouv.xboot.caching.GouvCache;

/**
 * 
 * Implémentation de l'interface MotifsCache
 * 
 * @author qdeme
 *
 */
public interface MotifsCache extends GouvCache<Integer, MotifDTO> {

    public MotifDTO getMotif(String codeMotif, String langue);
    
    public List<MotifDTO> getMotifs(String langue);
    
    public List<MotifDTO> getMotifs(String langue, DemandeStatutEnum statut);
    
}
