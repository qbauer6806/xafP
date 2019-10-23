package mc.gouv.xaf.back.service.motifs;

import java.util.List;

import mc.gouv.xaf.back.shared.dto.MotifDTO;
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
    
    public List<MotifDTO> getMotifs(String langue, String statut);

    public List<MotifDTO> getFilteredMotifs(String langue, List<String> codes);

}
