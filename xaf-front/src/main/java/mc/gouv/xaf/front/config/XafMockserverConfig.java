package mc.gouv.xaf.front.config;

import org.mockserver.integration.ClientAndServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"mockserver && !test"})
public class XafMockserverConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(XafMockserverConfig.class);

    @Value("${server.port}")
    private Integer serverPort;

    @Value("${mc.gouv.${application.name}.api.mockserverInitializerAbsolutePath}")
    private String mockserverInitializerAbsolutePath;

    @Bean
    public CommandLineRunner startEmploiMockServer() {
        return args -> {
            org.mockserver.configuration.Configuration configuration = new org.mockserver.configuration.Configuration();
            configuration.persistExpectations(true);
            configuration.watchInitializationJson(true);
            configuration.persistedExpectationsPath(mockserverInitializerAbsolutePath);
            configuration.initializationJsonPath(mockserverInitializerAbsolutePath);
            ClientAndServer.startClientAndServer(configuration, serverPort + 5);
            LOGGER.info("XafMockServer started on port {}", serverPort + 5);
        };
    }
}
