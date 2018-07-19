package mc.gouv.af.back;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import mc.gouv.Static;
import mc.gouv.af.back.cache.PaysCache;
import mc.gouv.af.back.cache.PaysCacheImpl;
import mc.gouv.af.back.cache.UsagersCache;
import mc.gouv.af.back.cache.UsagersCacheDataProvider;
import mc.gouv.af.back.cache.UsagersCacheImpl;
import mc.gouv.af.back.cache.UtilisateursCache;
import mc.gouv.af.back.cache.UtilisateursCacheImpl;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.logon.apiclient.LogonApiClient;
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
        String url = Static.getValue(LogonApiClient.DEFAULT_GOUV_PROPERTY_URL);
        return new UtilisateursCacheImpl(url, gouvPropertiesResolver.getDemarcheId(), UTILISATEURS_CACHE_DURATION);
    }
    
    @Bean(name = "usagersCacheImpl")
    public UsagersCache getUsagersCache() {
        return new UsagersCacheImpl(usagersCacheDataProvider, gouvPropertiesResolver.getUsagersCacheDuration());
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        builder.dateFormat(iso8601DateFormat);
        return builder;
    }
    
}
