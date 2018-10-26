package mc.gouv.af.back.config.jms;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisProperties;

/***
 * Configuration used to create the embedded Artemis server.**
 * 
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @author Phillip Webb
 */
class ArtemisEmbeddedSecureConfigurationFactory {

    private static final Log logger = LogFactory.getLog(ArtemisEmbeddedSecureConfigurationFactory.class);

    private final ArtemisProperties.Embedded properties;

    ArtemisEmbeddedSecureConfigurationFactory(ArtemisProperties properties) {
        this.properties = properties.getEmbedded();
    }

    public Configuration createConfiguration() {
        ConfigurationImpl configuration = new ConfigurationImpl();
        configuration.setSecurityEnabled(false);
        configuration.setPersistenceEnabled(this.properties.isPersistent());

        TransportConfiguration transportConfiguration = new TransportConfiguration(InVMAcceptorFactory.class.getName(),
                this.properties.generateTransportParameters());
        configuration.getAcceptorConfigurations().add(transportConfiguration);
        if (this.properties.isDefaultClusterPassword()) {
            logger.debug("Using default Artemis cluster password: " + this.properties.getClusterPassword());
        }
        configuration.setClusterPassword(this.properties.getClusterPassword());

        return configuration;
    }

}
