package mc.gouv.af.back;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import mc.gouv.af.back.util.LogonProxy;

/**
 * Classe de configuration Spring pour les tests unitaires
 * 
 * @author qdeme
 *
 */
@Configuration
@ComponentScan(basePackages="mc.gouv.af.back")
@Profile("test")
public class TestConfig {
    
    /**
     * DataSource "in memory" pour le besoin des tests
     * @return
     */
    @Bean
    @Primary
    public DataSource getDataSource() {
        return DataSourceBuilder.create().driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:derbymcembedded;MODE=DB2;USER=SA;DB_CLOSE_ON_EXIT=FALSE").build();
    }
    
    /**
     * Mock de LogonProxy pour les tests
     * @return
     */
    @Bean
    @Primary
    public LogonProxy getLogonProxy() {
        return new LogonProxyImplTest();
    }

}
