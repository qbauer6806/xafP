package mc.gouv.af.back;

import java.lang.management.ManagementFactory;

import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import mc.gouv.Static;

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
        String dbName = "ACTIVITI";
        String propertyPrefix = System.getProperty(dbName + "-PREFIX", dbName);

        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setMinIdle(Integer.parseInt(Static.getValue(propertyPrefix + "-MIN_IDLE", "0")));
        poolProperties.setMaxIdle(Integer.parseInt(Static.getValue(propertyPrefix + "-MAX_IDLE", "5")));
        poolProperties.setRemoveAbandoned(
                Boolean.parseBoolean(Static.getValue(propertyPrefix + "-REMOVE_ABANDONED", "false")));
        poolProperties.setRemoveAbandonedTimeout(
                Integer.parseInt(Static.getValue(propertyPrefix + "-REMOVE_ABANDONED_TIMEOUT", "60")));
        poolProperties.setMaxWait(Integer.parseInt(Static.getValue(propertyPrefix + "-MAX_WAIT", "5000")));
        poolProperties.setMaxActive(Integer.parseInt(Static.getValue(propertyPrefix + "-MAX_ACTIVE", "5")));
        poolProperties.setInitialSize(Integer.parseInt(Static.getValue(propertyPrefix + "-INITIAL_SIZE", "0")));
        if (Static.getValue(propertyPrefix + "-CONNECTION_PROPERTIES") != null) {
            poolProperties.setConnectionProperties(Static.getValue(propertyPrefix + "-CONNECTION_PROPERTIES"));
        }
        poolProperties.setDriverClassName(Static.getValue(propertyPrefix + "-CLASS"));
        poolProperties.setUrl(Static.getValue(propertyPrefix + "-URL"));
        poolProperties.setUsername(Static.getValue(propertyPrefix + "-USER"));
        poolProperties.setPassword(Static.getValue(propertyPrefix + "-PASS"));
        poolProperties.setJmxEnabled(true);
        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setPoolProperties(poolProperties);

        // register datasource in JMX for monitoring
        // http://www.programcreek.com/java-api-examples/index.php?api=org.apache.tomcat.jdbc.pool.DataSource
        final ConnectionPool pool = dataSource.createPool();
        org.apache.tomcat.jdbc.pool.jmx.ConnectionPool jmxPool = pool.getJmxPool();
        ObjectName objectName = ObjectName
                .getInstance(dataSource.getClass().getName() + ":context=demarches,name=" + dbName);
        ManagementFactory.getPlatformMBeanServer().registerMBean(jmxPool, objectName);

        return dataSource;
    }

}
