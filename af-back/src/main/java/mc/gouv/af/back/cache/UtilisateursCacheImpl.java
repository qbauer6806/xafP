package mc.gouv.af.back.cache;

public class UtilisateursCacheImpl extends mc.gouv.logon.caching.UtilisateursCacheImpl implements UtilisateursCache {

    public UtilisateursCacheImpl(String url, String codeAppli, long cacheDuration) {
        super(url, codeAppli, cacheDuration);
    }

}
