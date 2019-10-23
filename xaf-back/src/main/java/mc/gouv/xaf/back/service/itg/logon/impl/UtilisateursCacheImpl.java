package mc.gouv.xaf.back.service.itg.logon.impl;

import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;

public class UtilisateursCacheImpl extends mc.gouv.logon.caching.UtilisateursCacheImpl implements UtilisateursCache {

    public UtilisateursCacheImpl(String url, String codeAppli, long cacheDuration) {
        super(url, codeAppli, cacheDuration);
    }

}
