package mc.gouv.af.back.util;

public class UtilisateursCacheImpl extends mc.gouv.logon.caching.UtilisateursCacheImpl implements UtilisateursCache {

    public UtilisateursCacheImpl(String codeAppli, long cacheDuration) {
        super(codeAppli, cacheDuration);
    }

}
