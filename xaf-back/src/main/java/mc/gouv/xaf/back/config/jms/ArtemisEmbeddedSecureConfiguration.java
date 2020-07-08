package mc.gouv.xaf.back.config.jms;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.TopicConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.TopicConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisNoOpBindingRegistry;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

/***
 * Configuration used to create the embedded Artemis server.**
 * 
 * @author Eddú Meléndez
 * @author Phillip Webb
 * @author Stephane Nicoll
 */
@Configuration
@Conditional(IndexationEnabledCondition.class)
@ConditionalOnClass(name = ArtemisConnectionFactoryFactory.EMBEDDED_JMS_CLASS)
@ConditionalOnProperty(prefix = "spring.artemis.embedded.secure", name = "enabled", havingValue = "true", matchIfMissing = false)
class ArtemisEmbeddedSecureConfiguration {

    private final ArtemisProperties properties;

    private final List<ArtemisConfigurationCustomizer> configurationCustomizers;

    private final List<JMSQueueConfiguration> queuesConfiguration;

    private final List<TopicConfiguration> topicsConfiguration;

    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;

    ArtemisEmbeddedSecureConfiguration(ArtemisProperties properties,
            ObjectProvider<List<ArtemisConfigurationCustomizer>> configurationCustomizers,
            ObjectProvider<List<JMSQueueConfiguration>> queuesConfiguration,
            ObjectProvider<List<TopicConfiguration>> topicsConfiguration) {
        this.properties = properties;
        this.configurationCustomizers = configurationCustomizers.getIfAvailable();
        this.queuesConfiguration = queuesConfiguration.getIfAvailable();
        this.topicsConfiguration = topicsConfiguration.getIfAvailable();
    }

    @Bean
    @ConditionalOnMissingBean
    public org.apache.activemq.artemis.core.config.Configuration artemisConfiguration() {
        return new ArtemisEmbeddedSecureConfigurationFactory(this.properties).createConfiguration();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Conditional(CreateTopicCondition.class)
    @ConditionalOnMissingBean
    public EmbeddedJMS artemisServer(org.apache.activemq.artemis.core.config.Configuration configuration,
            JMSConfiguration jmsConfiguration) {
        EmbeddedJMS server = new EmbeddedJMS();
        customize(configuration);
        server.setConfiguration(configuration);
        server.setJmsConfiguration(jmsConfiguration);
        server.setRegistry(new ArtemisNoOpBindingRegistry());
        manageUsersAndRoles(server);
        return server;
    }

    private void manageUsersAndRoles(EmbeddedJMS server) {
        ArtemisSecurityManager securityManager = new ArtemisSecurityManager(gouvPropertiesResolver.getJmsSenderUser(),
                gouvPropertiesResolver.getJmsSenderPassword(), gouvPropertiesResolver.getJmsConsumerUser(),
                gouvPropertiesResolver.getJmsConsumerPassword());
        server.setSecurityManager(securityManager);
    }

    private void customize(org.apache.activemq.artemis.core.config.Configuration configuration) {
        if (this.configurationCustomizers != null) {
            AnnotationAwareOrderComparator.sort(this.configurationCustomizers);
            for (ArtemisConfigurationCustomizer customizer : this.configurationCustomizers) {
                customizer.customize(configuration);
            }
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public JMSConfiguration artemisJmsConfiguration() {
        JMSConfiguration configuration = new JMSConfigurationImpl();
        addAll(configuration.getQueueConfigurations(), this.queuesConfiguration);
        addAll(configuration.getTopicConfigurations(), this.topicsConfiguration);
        addQueues(configuration, this.properties.getEmbedded().getQueues());
        addTopics(configuration, this.properties.getEmbedded().getTopics());
        return configuration;
    }

    private <T> void addAll(List<T> list, Collection<? extends T> items) {
        if (items != null) {
            list.addAll(items);
        }
    }

    private void addQueues(JMSConfiguration configuration, String[] queues) {
        boolean persistent = this.properties.getEmbedded().isPersistent();
        for (String queue : queues) {
            JMSQueueConfigurationImpl jmsQueueConfiguration = new JMSQueueConfigurationImpl();
            jmsQueueConfiguration.setName(queue);
            jmsQueueConfiguration.setDurable(persistent);
            jmsQueueConfiguration.setBindings("/queue/" + queue);
            configuration.getQueueConfigurations().add(jmsQueueConfiguration);
        }
    }

    private void addTopics(JMSConfiguration configuration, String[] topics) {
        for (String topic : topics) {
            TopicConfigurationImpl topicConfiguration = new TopicConfigurationImpl();
            topicConfiguration.setName(topic);
            topicConfiguration.setBindings("/topic/" + topic);
            configuration.getTopicConfigurations().add(topicConfiguration);
        }
    }

}
