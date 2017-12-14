package mc.gouv.af.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import mc.gouv.af.back.service.properties.GouvPropertiesResolver;
import mc.gouv.af.back.util.PaysCache;
import mc.gouv.af.back.util.PaysCacheImpl;
import mc.gouv.af.back.util.UsagersCache;
import mc.gouv.af.back.util.UsagersCacheDataProvider;
import mc.gouv.af.back.util.UsagersCacheImpl;
import mc.gouv.af.back.util.UtilisateursCache;
import mc.gouv.af.back.util.UtilisateursCacheImpl;
import mc.gouv.servicerest.usager.ReferentielUsagersClient;

@Configuration
@EnableCaching
@Profile("gouv")
public class AfBackConfig {
    
    // 24h
    private static final long PAYS_CACHE_DURATION = 24*60*60*1000;
    
    // 6h
    private static final long UTILISATEURS_CACHE_DURATION = 6*60*60*1000;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private UsagersCacheDataProvider usagersCacheDataProvider;

    @Bean
    public ReferentielUsagersClient getReferentielUsagersClient() {

        return new ReferentielUsagersClient(gouvPropertiesResolver.getUsagersRestUrl(), null, null);
    }
    
    @Bean(name = "paysCacheImpl")
    public PaysCache getPaysCache() {
        return new PaysCacheImpl(gouvPropertiesResolver.getPaysRestUrl(), null, null, PAYS_CACHE_DURATION);
    }
    
    @Bean(name = "utilisateursCacheImpl")
    public UtilisateursCache getUtilisateursCache() {
        return new UtilisateursCacheImpl(gouvPropertiesResolver.getDemarcheId(), UTILISATEURS_CACHE_DURATION);
    }
    
    @Bean(name = "usagersCacheImpl")
    public UsagersCache getUsagersCache() {
        return new UsagersCacheImpl(usagersCacheDataProvider, gouvPropertiesResolver.getUsagersCacheDuration());
    }

}
