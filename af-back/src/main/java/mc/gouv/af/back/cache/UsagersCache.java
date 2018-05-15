package mc.gouv.af.back.cache;

import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.xboot.caching.GouvCache;

/**
 * 
 * Implémentation de l'interface UsagersCache
 * 
 * @author qdeme
 *
 */
public interface UsagersCache extends GouvCache<Integer, UsagerBean> {
    
}
