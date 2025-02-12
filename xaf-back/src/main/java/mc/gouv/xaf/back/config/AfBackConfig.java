package mc.gouv.xaf.back.config;

import java.text.SimpleDateFormat;

import org.activiti.compatibility.spring.DefaultFlowable5SpringCompatibilityHandler;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import jakarta.annotation.PostConstruct;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.nomen.PaysCache;
import mc.gouv.xaf.back.service.itg.nomen.impl.PaysCacheDataProvider;
import mc.gouv.xaf.back.service.itg.nomen.impl.PaysCacheImpl;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.itg.rest.impl.UsagersCacheDataProvider;
import mc.gouv.xaf.back.service.itg.rest.impl.UsagersCacheImpl;

/**
 * Classe de configuration
 *
 * @author qdeme
 */
@Configuration
@EnableCaching
public class AfBackConfig {

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Value("${display.name}")
    private String displayName;

    @Value("${logging.file.path}")
    private String loggingFile;

    @PostConstruct
    public void loadProperties() {
        System.setProperty("MC_LOGDIR", loggingFile);
        System.setProperty("MC_APPNAME", displayName);
    }

    @Bean(name = "paysCacheImpl")
    public PaysCache getPaysCache(PaysCacheDataProvider paysCacheDataProvider) {
        return new PaysCacheImpl(paysCacheDataProvider, gouvPropertiesResolver.getPaysCacheDuration());
    }

    @Bean(name = "usagersCacheImpl")
    public UsagersCache getUsagersCache(UsagersCacheDataProvider usagersCacheDataProvider) {
        return new UsagersCacheImpl(usagersCacheDataProvider, gouvPropertiesResolver.getUsagersCacheDuration());
    }

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        builder.dateFormat(iso8601DateFormat);
        return builder;
    }

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> enableFlowable5CompatibilityConfigurer() {
        return (SpringProcessEngineConfiguration processEngineConfiguration) -> {
            processEngineConfiguration.setFlowable5CompatibilityEnabled(true);
            processEngineConfiguration
                    .setFlowable5CompatibilityHandlerFactory(DefaultFlowable5SpringCompatibilityHandler::new);
        };

    }

}
