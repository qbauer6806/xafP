package mc.gouv.xaf.back.config;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.ToolManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VelocityConfig {

    @Bean
    public VelocityEngine velocityEngine() {
        VelocityEngine engine = new VelocityEngine();
        engine.init();
        return engine;
    }

    @Bean
    public ToolManager toolManager() {
        return new ToolManager();
    }
}
