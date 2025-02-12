package mc.gouv.xaf.back.service.itg.nomen.impl;

import mc.gouv.xaf.back.service.itg.nomen.PaysCache;
import mc.gouv.xaf.caching.GouvMemoryCache;
import mc.gouv.xaf.shared.dto.PaysDTO;

/**
 * Implémentation de l'interface PaysCache
 *
 * @author qdeme
 */
public class PaysCacheImpl extends GouvMemoryCache<String, PaysDTO> implements PaysCache {

    public PaysCacheImpl(PaysCacheDataProvider gouvCacheDataProvider, long cacheDuration) {
        super(gouvCacheDataProvider, cacheDuration);
    }

}
