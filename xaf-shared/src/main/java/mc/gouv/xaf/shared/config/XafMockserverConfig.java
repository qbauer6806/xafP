package mc.gouv.xaf.shared.config;

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

    @Value("${mc.gouv.mockserver.port}")
    private Integer mockserverPort;

    @Bean
    public CommandLineRunner startXafMockServer() {
        return args -> {
            ClientAndServer.startClientAndServer(mockserverPort);
            LOGGER.info("XafMockServer started on port {}", mockserverPort);
        };

    }
}
