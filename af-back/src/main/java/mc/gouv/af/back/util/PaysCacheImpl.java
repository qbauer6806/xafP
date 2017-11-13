package mc.gouv.af.back.util;

public class PaysCacheImpl extends mc.gouv.xboot.caching.commons.PaysNationalitesCache implements PaysCache {

    public PaysCacheImpl(String restUrl, String user, String password, long cacheDuration) {
        super(restUrl, user, password, cacheDuration);
    }

}
