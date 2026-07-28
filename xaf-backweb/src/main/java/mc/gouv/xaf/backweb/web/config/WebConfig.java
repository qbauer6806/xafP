package mc.gouv.xaf.backweb.web.config;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory;
import org.springframework.boot.web.error.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class WebConfig {

    @Value("${application.name}")
    private String applicationName;

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            factory.addServerCustomizers(server -> {
                // Expose Jetty managed beans to the JMX platform serverprovided by Spring
                final var mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
                server.addEventListener(mbContainer);
                server.addBean(mbContainer);
            });

            // Pour gérer derrière le proxy
            // X-Forwarded-Host: example.com
            // X-Forwarded-Proto: https
            factory.addServerCustomizers(server -> {
                for (Connector connector : server.getConnectors()) {
                    ConnectionFactory connectionFactory = connector.getDefaultConnectionFactory();
                    if (connectionFactory instanceof HttpConnectionFactory defaultConnectionFactory) {
                        var httpConfiguration = defaultConnectionFactory.getHttpConfiguration();
                        httpConfiguration.addCustomizer(new ForwardedRequestCustomizer());
                    }
                }
            });

            var error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/error/404");
            var error405Page = new ErrorPage(HttpStatus.METHOD_NOT_ALLOWED, "/error/405");
            var error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error/500");
            factory.addErrorPages(error404Page, error500Page, error405Page);
            factory.setDisplayName(applicationName.toUpperCase());

        };
    }

    /**
     * Configuration du mapper spring<->jackson pour les dates par défaut en ISO8601
     * https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/
     */
    @Bean
    @ConditionalOnMissingBean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder.defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
    }

}
