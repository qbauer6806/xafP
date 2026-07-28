package mc.gouv.xaf.back.config;

import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        // Depuis spring boot 4.1 il faut spécifier la version de jackson utilisée si il y a plusieurs versions dans le classpath (https://docs.hibernate.org/orm/7.3/whats-new/#jackson-3-support)
        return properties -> {
            properties.put("hibernate.type.json_format_mapper", "jackson3");
            properties.put("hibernate.type.xml_format_mapper", "jackson3");
        };
    }
}
