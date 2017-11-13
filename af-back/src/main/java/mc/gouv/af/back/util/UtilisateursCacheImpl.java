package mc.gouv.af.back.util;

public class UtilisateursCacheImpl extends mc.gouv.xboot.caching.commons.UtilisateursCache implements UtilisateursCache {

    public UtilisateursCacheImpl(String codeAppli, long cacheDuration) {
        super(codeAppli, cacheDuration);
    }

}
