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
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

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
public class GUKafkaConsumerConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaConsumerConfig.class);
	
	private final String XAF_GU_KAFKA_CONSUMER_BACKOFF_INTERVAL = "XAF_GU_KAFKA_CONSUMER_BACKOFF_INTERVAL";
	private final String XAF_GU_KAFKA_CONSUMER_BACKOFF_MAXATTEMPTS = "XAF_GU_KAFKA_CONSUMER_BACKOFF_MAXATTEMPTS";
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;
	
	@Autowired
	private PropertiesService propertiesService;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
    	System.out.println("consumerFactory()");
        Map<String, Object> props = new HashMap<>();
        props.put(
          ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
          gouvPropertiesResolver.getGUKafkaBootstrapServersConfig());
        
        // GroupID : le code appli (DemarcheID)
        props.put(
          ConsumerConfig.GROUP_ID_CONFIG, 
          gouvPropertiesResolver.getDemarcheId());
        props.put(
          ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
          StringDeserializer.class);
        props.put(
          ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
          StringDeserializer.class);
        //return new DefaultKafkaConsumerFactory<>(props);
        
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new ErrorHandlingDeserializer2<>(new StringDeserializer()));
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> 
      kafkaListenerContainerFactory() throws DemPropertyNotFoundException {
   
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
