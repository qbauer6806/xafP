package mc.gouv.xaf.back.service.itg.logon.impl;

import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.caching.GouvMemoryCache;

public class UtilisateursCacheImpl extends GouvMemoryCache<String, User> implements UtilisateursCache {

    public UtilisateursCacheImpl(UtilisateursCacheDataProvider gouvCacheDataProvider, long cacheDuration) {
        super(gouvCacheDataProvider, cacheDuration);
    }

}
