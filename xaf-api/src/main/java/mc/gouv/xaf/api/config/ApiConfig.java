package mc.gouv.xaf.api.config;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import mc.gouv.xaf.back.config.filter.RequestLoggingFilter;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import tools.jackson.databind.json.JsonMapper.Builder;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

@Configuration
public class ApiConfig {

    @Value("${application.name}")
    private String applicationName;

    @Value("${logging.file.path}")
    private String loggingFile;

    @PostConstruct
    public void loadProperties() {
        System.setProperty("MC_LOGDIR", loggingFile);
        System.setProperty("MC_APPNAME", applicationName.toUpperCase());
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

        static final class XafJackson2ObjectMapperBuilderCustomizer implements JsonMapperBuilderCustomizer, Ordered {

            @Override
            public int getOrder() {
                return 1;
            }

            @Override
            public void customize(Builder builder) {
                var filters = new SimpleFilterProvider().setFailOnUnknownId(false);

                builder.filterProvider(filters);

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
            factory.setDisplayName(applicationName);
        };
    }

}
