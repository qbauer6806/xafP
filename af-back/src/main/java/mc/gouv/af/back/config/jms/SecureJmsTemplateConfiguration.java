package mc.gouv.af.back.config.jms;

import javax.inject.Inject;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.dem.service.DemGouvPropertiesResolver;

@Configuration
@Conditional(IndexationEnabledCondition.class)
public class SecureJmsTemplateConfiguration {

    @Inject
    private DemGouvPropertiesResolver demGouvPropertiesResolver;

    @Bean
    public ActiveMQConnectionFactory activeMQSenderConnectionFactory() {
        String host = demGouvPropertiesResolver.getJmsHost();
        String port = String.valueOf(demGouvPropertiesResolver.getJmsPort());
        String user = demGouvPropertiesResolver.getJmsSenderUser();
        String password = demGouvPropertiesResolver.getJmsSenderPassword();

        return getActiveMQConnectionFactory(host, port, user, password);
    }

    @Bean
    public ActiveMQConnectionFactory activeMQConsumerConnectionFactory() {
        String host = demGouvPropertiesResolver.getJmsHost();
        String port = String.valueOf(demGouvPropertiesResolver.getJmsPort());
        String user = demGouvPropertiesResolver.getJmsConsumerUser();
        String password = demGouvPropertiesResolver.getJmsConsumerPassword();

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
        factory.setClientId(demGouvPropertiesResolver.getSubscriptionKey());
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
