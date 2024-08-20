package mc.gouv.xaf.xaf12batch.gichuni;

import mc.gouv.xboot.caching.GouvCache;
import mc.gouv.xboot.caching.GouvMemoryCache;

public class UsagersCacheImpl extends GouvMemoryCache<Integer, GichuniUsagerDTO> implements
        GouvCache<Integer, GichuniUsagerDTO> {

    public UsagersCacheImpl(UsagersCacheDataProvider gouvCacheDataProvider, long cacheDuration) {
        super(gouvCacheDataProvider, cacheDuration);
    }

}
