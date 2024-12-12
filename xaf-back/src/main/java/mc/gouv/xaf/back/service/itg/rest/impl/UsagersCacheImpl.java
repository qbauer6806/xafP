package mc.gouv.xaf.back.service.itg.rest.impl;

import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.caching.GouvMemoryCache;

/**
 * Implémentation de l'interface UsagersCache
 *
 * @author qdeme
 */
public class UsagersCacheImpl extends GouvMemoryCache<Integer, GichuniUsagerDTO> implements UsagersCache {

    public UsagersCacheImpl(UsagersCacheDataProvider gouvCacheDataProvider, long cacheDuration) {
        super(gouvCacheDataProvider, cacheDuration);
    }

}
