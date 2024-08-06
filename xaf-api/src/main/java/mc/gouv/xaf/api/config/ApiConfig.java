package mc.gouv.xaf.api.config;

import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import mc.gouv.xaf.api.date.util.ISO8601WithMillisDateFormat;
import mc.gouv.xaf.back.config.filter.RequestLoggingFilter;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class ApiConfig {

    @Value("${display.name}")
    private String displayName;

    @Value("${logging.file.path}")
    private String loggingFile;

    @PostConstruct
    public void loadProperties() {
        System.setProperty("MC_LOGDIR", loggingFile) ;
        System.setProperty("MC_APPNAME", displayName) ;
    }

    @Bean
    public RequestLoggingFilter logFilter() {
        return new RequestLoggingFilter();
    }

    @Configuration
    static class Jackson2ObjectMapperBuilderCustomizerConfiguration {

        @Bean
        public XafJackson2ObjectMapperBuilderCustomizer xafJackson2ObjectMapperBuilderCustomizer() {
            return new XafJackson2ObjectMapperBuilderCustomizer();
        }

        static final class XafJackson2ObjectMapperBuilderCustomizer
                implements Jackson2ObjectMapperBuilderCustomizer, Ordered {

            @Override
            public void customize(Jackson2ObjectMapperBuilder builder) {
                builder.dateFormat(new ISO8601WithMillisDateFormat());

                // Ajout de cette configuration par défaut our ne pas avoir d'exception si des entités ont des
                // annotations @JsonFilter sans configuration de filter associé
                var filters = new SimpleFilterProvider();
                filters.setFailOnUnknownId(false);
                builder.filters(filters);
            }

            @Override
            public int getOrder() {
                return 1;
            }
        }
    }
    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            factory.addServerCustomizers(server -> {
                // Expose Jetty managed beans to the JMX platform server provided by Spring
                final var mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
                server.addEventListener(mbContainer);
                server.addBean(mbContainer);
            });
            factory.setDisplayName(displayName);
        };
    }

}
