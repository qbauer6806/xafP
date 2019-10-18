package mc.gouv.xaf.back.config.jms;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

/****
 * {**
 * 
 * @link EnableAutoConfiguration Auto-configuration} to integrate with an Artemis broker. If the necessary classes are
 *       present, embed the broker in the application by default. Otherwise, connect to a broker available on the local
 *       machine with the default settings.
 *
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @since 1.3.0
 * @see ArtemisProperties
 */
@Configuration
@Conditional(IndexationEnabledCondition.class)
@AutoConfigureBefore(JmsAutoConfiguration.class)
@AutoConfigureAfter({ JndiConnectionFactoryAutoConfiguration.class })
@ConditionalOnClass({ ConnectionFactory.class, ActiveMQConnectionFactory.class })

//@ConditionalOnMissingBean(ConnectionFactory.class)
@EnableConfigurationProperties(ArtemisProperties.class)
@Import({ ArtemisEmbeddedSecureConfiguration.class, ArtemisConnectionFactoryConfiguration.class })
public class ArtemisAutoConfiguration {

    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Bean
    public ActiveMQConnectionFactory activeMQSenderConnectionFactory() {
        String host = gouvPropertiesResolver.getJmsHost();
        String port = String.valueOf(gouvPropertiesResolver.getJmsPort());
        String user = gouvPropertiesResolver.getJmsSenderUser();
        String password = gouvPropertiesResolver.getJmsSenderPassword();

        return getActiveMQConnectionFactory(host, port, user, password);
    }

    @Bean
    public ActiveMQConnectionFactory activeMQConsumerConnectionFactory() {
        String host = gouvPropertiesResolver.getJmsHost();
        String port = String.valueOf(gouvPropertiesResolver.getJmsPort());
        String user = gouvPropertiesResolver.getJmsConsumerUser();
        String password = gouvPropertiesResolver.getJmsConsumerPassword();

        return getActiveMQConnectionFactory(host, port, user, password);
    }

    private ActiveMQConnectionFactory getActiveMQConnectionFactory(String host, String port, String user,
            String password) {
        ActiveMQConnectionFactory activeConnectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);
        activeConnectionFactory.setUser(user);
        activeConnectionFactory.setPassword(password);

        return activeConnectionFactory;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        factory.setPubSubDomain(true);
        factory.setSubscriptionDurable(true);
        factory.setClientId(gouvPropertiesResolver.getSubscriptionKey());
        // This provides all boot's default to this factory, including the message converter
        configurer.configure(factory, activeMQConsumerConnectionFactory());
        // You could still override some of Boot's default if necessary.
        return factory;
    }

    @Bean
    public JmsTemplate createJmsTemplate() throws Exception {

        JmsTemplate jmsTemplate = new JmsTemplate(activeMQSenderConnectionFactory());
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
        return jmsTemplate;
    }

    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
