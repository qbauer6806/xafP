package mc.gouv.af.back.config.jms;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisMode;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisProperties;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/***
 * Factory to create an Artemis{
 * 
 * @link ActiveMQConnectionFactory} instance from properties defined in {@link ArtemisProperties}.
 *
 * @author Eddú Meléndez
 * @author Phillip Webb
 * @author Stephane Nicoll
 */
class ArtemisConnectionFactoryFactory {

    static final String EMBEDDED_JMS_CLASS = "org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS";

    private final ArtemisProperties properties;

    private final ListableBeanFactory beanFactory;

    ArtemisConnectionFactoryFactory(ListableBeanFactory beanFactory, ArtemisProperties properties) {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        Assert.notNull(properties, "Properties must not be null");
        this.beanFactory = beanFactory;
        this.properties = properties;
    }

    public <T extends ActiveMQConnectionFactory> T createConnectionFactory(Class<T> factoryClass) {
        try {
            startEmbeddedJms();
            return doCreateConnectionFactory(factoryClass);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create " + "ActiveMQConnectionFactory", ex);
        }
    }

    private void startEmbeddedJms() {
        if (ClassUtils.isPresent(EMBEDDED_JMS_CLASS, null)) {
            try {
                this.beanFactory.getBeansOfType(Class.forName(EMBEDDED_JMS_CLASS));
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    private <T extends ActiveMQConnectionFactory> T doCreateConnectionFactory(Class<T> factoryClass) throws Exception {
        ArtemisMode mode = this.properties.getMode();
        if (mode == null) {
            mode = deduceMode();
        }
        if (mode == ArtemisMode.EMBEDDED) {
            return createEmbeddedConnectionFactory(factoryClass);
        }
        return createNativeConnectionFactory(factoryClass);
    }

    /**
     * Deduce the {@link ArtemisMode} to use if none has been set.
     *
     * @return the mode
     */
    private ArtemisMode deduceMode() {
        if (this.properties.getEmbedded().isEnabled() && ClassUtils.isPresent(EMBEDDED_JMS_CLASS, null)) {
            return ArtemisMode.EMBEDDED;
        }
        return ArtemisMode.NATIVE;
    }

    private <T extends ActiveMQConnectionFactory> T createEmbeddedConnectionFactory(Class<T> factoryClass)
            throws Exception {
        try {
            TransportConfiguration transportConfiguration = new TransportConfiguration(
                    InVMConnectorFactory.class.getName(), this.properties.getEmbedded().generateTransportParameters());
            ServerLocator serviceLocator = ActiveMQClient.createServerLocatorWithoutHA(transportConfiguration);
            return factoryClass.getConstructor(ServerLocator.class).newInstance(serviceLocator);
        } catch (NoClassDefFoundError ex) {
            throw new IllegalStateException("Unable to create InVM "
                    + "Artemis connection, ensure that artemis-jms-server.jar " + "is in the classpath", ex);
        }
    }

    private <T extends ActiveMQConnectionFactory> T createNativeConnectionFactory(Class<T> factoryClass)
            throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(TransportConstants.HOST_PROP_NAME, this.properties.getHost());
        params.put(TransportConstants.PORT_PROP_NAME, this.properties.getPort());
        TransportConfiguration transportConfiguration = new TransportConfiguration(
                NettyConnectorFactory.class.getName(), params);
        Constructor<T> constructor = factoryClass.getConstructor(boolean.class, TransportConfiguration[].class);
        T connectionFactory = constructor.newInstance(false, new TransportConfiguration[] { transportConfiguration });
        String user = this.properties.getUser();
        if (StringUtils.hasText(user)) {
            connectionFactory.setUser(user);
            connectionFactory.setPassword(this.properties.getPassword());
        }
        return connectionFactory;
    }

}
