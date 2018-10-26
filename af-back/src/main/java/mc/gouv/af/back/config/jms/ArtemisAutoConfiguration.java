package mc.gouv.af.back.config.jms;

import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;

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
@ConditionalOnMissingBean(ConnectionFactory.class)
@EnableConfigurationProperties(ArtemisProperties.class)
@Import({ ArtemisEmbeddedSecureConfiguration.class, ArtemisConnectionFactoryConfiguration.class })
public class ArtemisAutoConfiguration {

}
