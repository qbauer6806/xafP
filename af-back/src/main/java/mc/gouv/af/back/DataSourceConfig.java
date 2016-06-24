package mc.gouv.af.back;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Classe de configuration Spring de la DataSource
 * 
 * @author qdeme
 *
 */
@Configuration
@EnableAutoConfiguration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource getDataSource() throws Exception {
        System.out.println("AAAAAAAA0");
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setDriverClassName("org.h2.Driver");
        poolProperties.setUrl("jdbc:h2:tcp://190.1.37.34/~/activiti;MV_STORE=FALSE;MVCC=FALSE");
        poolProperties.setUsername("sa");
        poolProperties.setPassword("");
        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setPoolProperties(poolProperties);
        return dataSource;
    }

}
