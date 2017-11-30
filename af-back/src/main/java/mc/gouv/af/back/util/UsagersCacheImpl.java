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

    public UsagersCacheImpl(UsagersCacheDataProvider gouvCacheDataProvider, long cacheDuration) {
        super(gouvCacheDataProvider, cacheDuration);
    }

}
