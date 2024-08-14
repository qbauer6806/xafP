package mc.gouv.xaf.xaf12batch;

import mc.gouv.servicerest.caching.PaysNationalitesCache;
import mc.gouv.xaf.xaf12batch.pays.PaysCacheImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${pays.url}")
    private String paysUrl;

    // 24h
    private static final long PAYS_CACHE_DURATION = 24 * 60 * 60 * 1000L;

    @Bean(name = "paysCacheImpl")
    public PaysNationalitesCache getPaysCache() {
        return new PaysCacheImpl(paysUrl, null, null, PAYS_CACHE_DURATION);
    }

}


