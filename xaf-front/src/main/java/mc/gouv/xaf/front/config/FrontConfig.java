package mc.gouv.xaf.front.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class FrontConfig {

    @Value("${application.name}")
    private String applicationName;

    @Value("${logging.file.path}")
    private String loggingFile;

    @PostConstruct
    public void loadProperties() {
        System.setProperty("MC_LOGDIR", loggingFile);
        System.setProperty("MC_APPNAME", applicationName.toUpperCase());
    }
}
