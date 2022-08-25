package mc.gouv.xaf.back.service.itg.rest.impl;

import org.springframework.context.annotation.Profile;

import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xboot.caching.GouvMemoryCache;

/**
 * 
 * Implémentation de l'interface UsagersCache
 * 
 * @author qdeme
 *
 */
@Profile("gouv")
public class UsagersCacheImpl extends GouvMemoryCache<Integer, GichuniUsagerDTO> implements UsagersCache {

    public UsagersCacheImpl(UsagersCacheDataProvider gouvCacheDataProvider, long cacheDuration) {
        super(gouvCacheDataProvider, cacheDuration);
    }

}
