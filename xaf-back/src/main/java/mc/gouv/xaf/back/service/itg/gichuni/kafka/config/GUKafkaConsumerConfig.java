package mc.gouv.xaf.back.service.itg.gichuni.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import mc.gouv.xaf.back.config.KafkaEnabledCondition;
import mc.gouv.xaf.back.properties.DemPropertyNotFoundException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

/**
 * 
 * Configuration du Consumer Kafka pour le Guichet Unique
 * 
 * @author qdeme
 *
 */
@EnableKafka
@Configuration
@Conditional(KafkaEnabledCondition.class)
public class GUKafkaConsumerConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaConsumerConfig.class);
	
	private static final String XAF_GU_KAFKA_CONSUMER_BACKOFF_INTERVAL = "XAF_GU_KAFKA_CONSUMER_BACKOFF_INTERVAL";
	private static final String XAF_GU_KAFKA_CONSUMER_BACKOFF_MAXATTEMPTS = "XAF_GU_KAFKA_CONSUMER_BACKOFF_MAXATTEMPTS";
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
    	LOGGER.info("Création du GUKafkaConsumer...");
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, gouvPropertiesResolver.getGUKafkaBootstrapServersConfig());
        
        // GroupID : le code appli (DemarcheID)
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, gouvPropertiesResolver.getDemarcheId());
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        configProps.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, gouvPropertiesResolver.getGUKafkaConsumerFetchMaxBytes()); 
        configProps.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, gouvPropertiesResolver.getGUKafkaConsumerMaxPartitionFetchBytes());
        
        boolean sslEnabled = gouvPropertiesResolver.getGUKafkaSSLEnabled();
        if (sslEnabled) {        	
        	configProps.put("security.protocol", "SSL");
        	
        	configProps.put("ssl.truststore.location", gouvPropertiesResolver.getGUKafkaSSLTrustStoreLocation());
        	configProps.put("ssl.truststore.password", gouvPropertiesResolver.getGUKafkaSSLTrustStorePassword());
        	configProps.put("ssl.key.password", gouvPropertiesResolver.getGUKafkaSSLKeyStorePassword());
        	configProps.put("ssl.keystore.password", gouvPropertiesResolver.getGUKafkaSSLKeyStorePassword());
        	configProps.put("ssl.keystore.location", gouvPropertiesResolver.getGUKafkaSSLKeyStoreLocation());
        	configProps.put("ssl.endpoint.identification.algorithm", "");
        }
        
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new StringDeserializer()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> 
      kafkaListenerContainerFactory(KafkaTemplate<String, String> kafkaTemplate, PropertiesService propertiesService) throws DemPropertyNotFoundException {
   
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
          new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Afin d'éviter que l'appli s'arrête (crash total) lorsque Kafka tombe
        factory.setMissingTopicsFatal(false);
        
        LOGGER.info("Récupération des DEM_PROPERTIES en base pour le GUKafkaConsumerConfig...");
        PropertiesDTO backOffIntervalProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_GU_KAFKA_CONSUMER_BACKOFF_INTERVAL);
        if (backOffIntervalProp == null || StringUtils.isBlank(backOffIntervalProp.getValue())) {
        	throw new DemPropertyNotFoundException(XAF_GU_KAFKA_CONSUMER_BACKOFF_INTERVAL);
        }
        Integer backOffInterval = Integer.parseInt(backOffIntervalProp.getValue());
        PropertiesDTO backOffMaxAttemptsProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_GU_KAFKA_CONSUMER_BACKOFF_MAXATTEMPTS);
        if (backOffMaxAttemptsProp == null || StringUtils.isBlank(backOffMaxAttemptsProp.getValue())) {
        	throw new DemPropertyNotFoundException(XAF_GU_KAFKA_CONSUMER_BACKOFF_MAXATTEMPTS);
        }
        Integer backOffMaxAttempts = Integer.parseInt(backOffMaxAttemptsProp.getValue());
        
        BackOff bo = new FixedBackOff(backOffInterval, backOffMaxAttempts);
        factory.setErrorHandler(new SeekToCurrentErrorHandler(
        	      new DeadLetterPublishingRecoverer(kafkaTemplate), bo));
        
        return factory;
    }
}
