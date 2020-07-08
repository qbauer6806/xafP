package mc.gouv.xaf.back.config.jms;

import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;

/***
 * Configuration for Artemis{
 * 
 * @link ConnectionFactory}.
 *
 * @author Eddú Meléndez
 * @author Phillip Webb
 */
@Configuration
@Conditional(IndexationEnabledCondition.class)
@ConditionalOnMissingBean(ConnectionFactory.class)
class ArtemisConnectionFactoryConfiguration {

    @Bean
    public ActiveMQConnectionFactory jmsConnectionFactory(ListableBeanFactory beanFactory,
            ArtemisProperties properties) {
        return new ArtemisConnectionFactoryFactory(beanFactory, properties)
                .createConnectionFactory(ActiveMQConnectionFactory.class);
    }

}
