package mc.gouv.xaf.backweb.web.config;

import java.lang.management.ManagementFactory;

import javax.servlet.http.HttpSessionListener;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.util.log.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import mc.gouv.tools.monitor.servlets.MonitorServlet;
import mc.gouv.tools.monitor.servlets.SessionCounter;

@Configuration
public class WebConfig {

    @Value("${display.name}")
    private String displayName;

    @Bean
    public ServletRegistrationBean monitorServletRegistration() {
        var registrationBean = new ServletRegistrationBean(new MonitorServlet());
        registrationBean.addUrlMappings("/monitor/*");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }

    @Bean
    public HttpSessionListener sessionCounterListener() {
        return new SessionCounter();
    }

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> WebServerFactoryCustomizer() {
        return factory -> {
            factory.addServerCustomizers(server -> {
                // Expose Jetty managed beans to the JMX platform serverprovided by Spring
                final var mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
                server.addEventListener(mbContainer);
                server.addBean(mbContainer);
                // Add loggers MBean to server (will be picked up by MBeanContainer above)
                server.addBean(Log.getLog());
            });

            // Pour gérer derrière le proxy
            // X-Forwarded-Host: example.com
            // X-Forwarded-Proto: https
            factory.addServerCustomizers(server -> {
                for (Connector connector : server.getConnectors()) {
                    ConnectionFactory connectionFactory = connector.getDefaultConnectionFactory();
                    if (connectionFactory instanceof HttpConnectionFactory) {
                        var defaultConnectionFactory = (HttpConnectionFactory) connectionFactory;
                        var httpConfiguration = defaultConnectionFactory.getHttpConfiguration();
                        httpConfiguration.addCustomizer(new ForwardedRequestCustomizer());
                    }
                }
            });

            var error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/error/404");
            var error405Page = new ErrorPage(HttpStatus.METHOD_NOT_ALLOWED, "/error/405");
            var error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error/500");
            factory.addErrorPages(error404Page, error500Page, error405Page);
            factory.setDisplayName(displayName);

        };
    }

    /**
     * Configuration du mapper spring<->jackson pour les dates par défaut en ISO8601
     * https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/
     */
    @Bean
    @ConditionalOnMissingBean(Jackson2ObjectMapperBuilder.class)
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        var builder = new Jackson2ObjectMapperBuilder();
        builder.dateFormat(new ISO8601DateFormat());
        return builder;
    }

}
