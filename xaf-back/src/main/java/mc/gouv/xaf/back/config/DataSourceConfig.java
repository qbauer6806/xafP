package mc.gouv.xaf.back.config;

import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.management.*;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;

@Profile("gouv")
@Configuration
public class DataSourceConfig {

    @Value("${application.name}")
    private String applicationName;

    @Value("${display.name}")
    private String displayName;

    // Obligatoire
    @Value("${mc.gouv.${application.name}.database.class}")
    private String driver;

    @Value("${mc.gouv.${application.name}.database.url}")
    private String url;

    @Value("${mc.gouv.${application.name}.database.user}")
    private String user;

    @Value("${mc.gouv.${application.name}.database.pass}")
    private String pass;

    // Optionnel
    @Value("${mc.gouv.database.minIddle:0}")
    private String minIddle;

    @Value("${mc.gouv.database.maxIddle:5}")
    private String maxIddle;

    @Value("${mc.gouv.database.removeAbandoned:false}")
    private String removeAbandoned;

    @Value("${mc.gouv.database.removeAbandonedTimout:60}")
    private String removeAbandonedTimout;

    @Value("${mc.gouv.database.maxWait:5000}")
    private String maxWait;

    @Value("${mc.gouv.database.maxActive:5}")
    private String maxActive;

    @Value("${mc.gouv.database.initialSize:0}")
    private String initialSize;

    @Value("${mc.gouv.database.connectionProperties:}")
    private String connectionProperties;

    @Value("${mc.gouv.database.testOnBorrow:true}")
    private String testOnBorrow;

    @Value("${mc.gouv.database.validationQuery:SELECT 1}")
    private String validationQuery;

    @Value("${mc.gouv.database.validationInterval:30000}")
    private String validationInterval;

    @Value("${mc.gouv.database.testWhileIddle:true}")
    private String testWhileIddle;

    @Value("${mc.gouv.database.timeBetweenEvictionRunsMillis:60000}")
    private String timeBetweenEvictionRunsMillis;

    /**
     * Méthode permettant la récupération de la datasource princiale
     * @return Datasource créée
     */
    @Bean(name = "dataSource")
    @Primary
    public DataSource getDataSource() throws SQLException, MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        return getDataSource(StringUtils.upperCase(applicationName), displayName);
    }

    /**
     * Méthode permettant de récupérer la datasource
     * @param dbName Nom de la base de données
     * @param displayName Nom de la variable JMX
     * @return Datasource créée
     */
    public DataSource getDataSource(String dbName, String displayName) throws SQLException, MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        var poolProperties = new PoolProperties();
        poolProperties.setMinIdle(Integer.parseInt(minIddle));
        poolProperties.setMaxIdle(Integer.parseInt(maxIddle));
        poolProperties.setRemoveAbandoned(Boolean.parseBoolean(removeAbandoned));
        poolProperties.setRemoveAbandonedTimeout(Integer.parseInt(removeAbandonedTimout));
        poolProperties.setMaxWait(Integer.parseInt(maxWait));
        poolProperties.setMaxActive(Integer.parseInt(maxActive));
        poolProperties.setInitialSize(Integer.parseInt(initialSize));
        if (StringUtils.isNotBlank(connectionProperties)) {
            poolProperties.setConnectionProperties(connectionProperties);
        }
        poolProperties.setDriverClassName(driver);
        poolProperties.setUrl(url);
        poolProperties.setUsername(user);
        poolProperties.setPassword(pass);
        poolProperties.setJmxEnabled(true);
        
        // Tolérance aux pannes DB (reconnexion) :
        poolProperties.setTestOnBorrow(Boolean.parseBoolean(testOnBorrow));
        poolProperties.setValidationQuery(validationQuery);
        poolProperties.setValidationInterval(Integer.parseInt(validationInterval));
        poolProperties.setTestWhileIdle(Boolean.parseBoolean(testWhileIddle));
        poolProperties.setTimeBetweenEvictionRunsMillis(Integer.parseInt(timeBetweenEvictionRunsMillis));
        
        var dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setPoolProperties(poolProperties);

        // register datasource in JMX for monitoring
        // http://www.programcreek.com/java-api-examples/index.php?api=org.apache.tomcat.jdbc.pool.DataSource
        final ConnectionPool pool = dataSource.createPool();
        var jmxPool = pool.getJmxPool();

        var objectName = ObjectName.getInstance(
                dataSource.getClass().getName() + ":context=" + StringUtils.lowerCase(displayName) + ",name=" + dbName);
        ManagementFactory.getPlatformMBeanServer().registerMBean(jmxPool, objectName);

        return dataSource;
    }
}
