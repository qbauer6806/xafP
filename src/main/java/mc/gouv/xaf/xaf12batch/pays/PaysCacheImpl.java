package mc.gouv.xaf.xaf12batch.pays;

import mc.gouv.servicerest.caching.PaysNationalitesCacheImpl;

public class PaysCacheImpl extends PaysNationalitesCacheImpl {

    public PaysCacheImpl(String restUrl, String user, String password, long cacheDuration) {
        super(restUrl, user, password, cacheDuration);
    }

}
