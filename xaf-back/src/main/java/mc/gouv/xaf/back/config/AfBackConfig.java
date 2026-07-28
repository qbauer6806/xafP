package mc.gouv.xaf.back.config;

import jakarta.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.impl.UtilisateursCacheDataProvider;
import mc.gouv.xaf.back.service.itg.logon.impl.UtilisateursCacheImpl;
import mc.gouv.xaf.back.service.itg.nomen.PaysCache;
import mc.gouv.xaf.back.service.itg.nomen.impl.PaysCacheDataProvider;
import mc.gouv.xaf.back.service.itg.nomen.impl.PaysCacheImpl;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.itg.rest.impl.UsagersCacheDataProvider;
import mc.gouv.xaf.back.service.itg.rest.impl.UsagersCacheImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Classe de configuration
 *
 * @author qdeme
 */
@Configuration
@RequiredArgsConstructor
public class AfBackConfig {

    private final GouvPropertiesResolver gouvPropertiesResolver;

    @Value("${application.name}")
    private String applicationName;

    @Value("${logging.file.path}")
    private String loggingFile;

    @PostConstruct
    public void loadProperties() {
        System.setProperty("MC_LOGDIR", loggingFile);
        System.setProperty("MC_APPNAME", applicationName.toUpperCase());
    }

    @Bean(name = "paysCacheImpl")
    public PaysCache getPaysCache(PaysCacheDataProvider paysCacheDataProvider) {
        return new PaysCacheImpl(paysCacheDataProvider, gouvPropertiesResolver.getPaysCacheDuration());
    }

    @Bean(name = "usagersCacheImpl")
    public UsagersCache getUsagersCache(UsagersCacheDataProvider usagersCacheDataProvider) {
        return new UsagersCacheImpl(usagersCacheDataProvider, gouvPropertiesResolver.getUsagersCacheDuration());
    }

    @Bean(name = "utilisateursCacheImpl")
    public UtilisateursCache getUtilisateursCache(UtilisateursCacheDataProvider utilisateursCacheDataProvider) {
        return new UtilisateursCacheImpl(utilisateursCacheDataProvider,
                gouvPropertiesResolver.getUtilisateursCacheDuration());
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
