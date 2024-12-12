package mc.gouv.xaf.back.service.itg.rest.impl;

import mc.gouv.servicerest.caching.PaysNationalitesCacheImpl;
import mc.gouv.xaf.back.service.itg.rest.PaysCache;

public class PaysCacheImpl extends PaysNationalitesCacheImpl implements PaysCache {

    public PaysCacheImpl(String restUrl, String user, String password, long cacheDuration) {
        super(restUrl, user, password, cacheDuration);
    }

}
