package mc.gouv.xaf.back.service.itg.rest;

import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.caching.GouvCache;

/**
 * 
 * Implémentation de l'interface UsagersCache
 * 
 * @author qdeme
 *
 */
public interface UsagersCache extends GouvCache<Integer, GichuniUsagerDTO> {
    
}
