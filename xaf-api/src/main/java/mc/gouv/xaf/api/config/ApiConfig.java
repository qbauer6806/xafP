package mc.gouv.xaf.api.config;

import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSessionListener;

import mc.gouv.xaf.back.config.filter.RequestLoggingFilter;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.util.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import mc.gouv.tools.monitor.servlets.MonitorServlet;
import mc.gouv.tools.monitor.servlets.SessionCounter;
import mc.gouv.xaf.api.date.util.ISO8601WithMillisDateFormat;

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
    public ServletRegistrationBean monitorServletRegistration() {
        var registrationBean = new ServletRegistrationBean(new MonitorServlet());
        registrationBean.addUrlMappings("/monitor/*");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
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
    public HttpSessionListener sessionCounterListener() {
        return new SessionCounter();
    }

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> WebServerFactoryCustomizer() {
        return factory -> {
            factory.addServerCustomizers(server -> {
                // Expose Jetty managed beans to the JMX platform server provided by Spring
                final var mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
                server.addEventListener(mbContainer);
                server.addBean(mbContainer);
                // Add loggers MBean to server (will be picked up by MBeanContainer above)
                // TODO deprecated
                server.addBean(Log.getLog());
            });
            factory.setDisplayName(displayName);
        };
    }

}
