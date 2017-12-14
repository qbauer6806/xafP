package mc.gouv.af.back.util;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.servicerest.caching.PaysNationalitesCache;

@Profile("gouv")
@Component
public class PaysCacheImpl extends PaysNationalitesCache implements PaysCache {

    public PaysCacheImpl(String restUrl, String user, String password, long cacheDuration) {
        super(restUrl, user, password, cacheDuration);
    }
    
}
