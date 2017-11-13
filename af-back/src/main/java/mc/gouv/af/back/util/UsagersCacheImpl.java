package mc.gouv.af.back.util;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.xboot.caching.GouvMemoryCache;

/**
 * 
 * Implémentation de l'interface UsagersCache
 * 
 * @author qdeme
 *
 */
@Profile("gouv")
@Component
public class UsagersCacheImpl extends GouvMemoryCache<Integer, UsagerBean> implements UsagersCache {
    
    // 3 heures
    private static final long CACHE_DURATION = 3*60*60*1000;

    public UsagersCacheImpl(UsagersCacheDataProvider gouvCacheDataProvider) {
        super(gouvCacheDataProvider, CACHE_DURATION);
    }

}
