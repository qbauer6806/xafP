package mc.gouv.xaf.back.service.itg.gichuni.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.scheduling.annotation.EnableAsync;

import mc.gouv.xaf.back.config.KafkaEnabledCondition;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.impl.GUKafkaProducerListener;

/**
 * 
 * Configuration du Producer Kafka pour le Guichet Unique
 * 
 * @author qdeme
 *
 */
@EnableKafka
@EnableAsync
@Configuration
@Conditional(KafkaEnabledCondition.class)
public class GUKafkaProducerConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaProducerConfig.class);
	
	@Autowired
	private GUKafkaProducerListener guKafkaProducerListener;
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
          ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
          gouvPropertiesResolver.getGUKafkaBootstrapServersConfig());
        configProps.put(
          ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
          StringSerializer.class);
        configProps.put(
          ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
          StringSerializer.class);
        
        boolean sslEnabled = gouvPropertiesResolver.getGUKafkaSSLEnabled();
        if (sslEnabled) {        	
        	configProps.put("security.protocol", "SSL");
        	
        	configProps.put("ssl.truststore.location", gouvPropertiesResolver.getGUKafkaSSLTrustStoreLocation());
        	configProps.put("ssl.truststore.password", gouvPropertiesResolver.getGUKafkaSSLTrustStorePassword());
        	configProps.put("ssl.key.password", gouvPropertiesResolver.getGUKafkaSSLKeyStorePassword());
        	configProps.put("ssl.keystore.password", gouvPropertiesResolver.getGUKafkaSSLKeyStorePassword());
        	configProps.put("ssl.keystore.location", gouvPropertiesResolver.getGUKafkaSSLKeyStoreLocation());
        }
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
    	KafkaTemplate<String, String> kt = new KafkaTemplate<>(producerFactory());
    	kt.setProducerListener(guKafkaProducerListener);
        return kt;
    }
}
