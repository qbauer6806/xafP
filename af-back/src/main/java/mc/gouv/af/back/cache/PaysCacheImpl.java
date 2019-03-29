package mc.gouv.af.back.cache;

import org.springframework.context.annotation.Profile;

import mc.gouv.servicerest.caching.PaysNationalitesCacheImpl;

@Profile("gouv")
public class PaysCacheImpl extends PaysNationalitesCacheImpl implements PaysCache {

    public PaysCacheImpl(String restUrl, String user, String password, long cacheDuration) {
        super(restUrl, user, password, cacheDuration);
    }

}
