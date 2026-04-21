package mc.gouv.xaf.back.service.itg.gichuni.kafka.config;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.properties.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Configuration du Consumer Kafka pour le Guichet Unique
 *
 * @author qdeme
 */
@EnableKafka
@Configuration
@ConditionalOnExpression(value = "'${mc.gouv.appli.shared.backapi.kafka.enabled}' == 'true'")
@RequiredArgsConstructor
public class GUKafkaConsumerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaConsumerConfig.class);

    private final GouvPropertiesResolver gouvPropertiesResolver;
    private final KafkaProperties kafkaProperties;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        LOGGER.info("Création du GUKafkaConsumer...");
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServersConfig());

        // GroupID : le code appli (DemarcheID)
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, gouvPropertiesResolver.getDemarcheId());
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        configProps.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, kafkaProperties.getFetchMaxBytes());
        configProps.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, kafkaProperties.getMaxPartitionFetchBytes());

        boolean sslEnabled = kafkaProperties.isKafkaSSLEnabled();
        if (sslEnabled) {
            configProps.put("security.protocol", "SSL");

            configProps.put("ssl.truststore.location", kafkaProperties.getTruststoreLocation());
            configProps.put("ssl.truststore.password", kafkaProperties.getTruststorePassword());
            configProps.put("ssl.key.password", kafkaProperties.getKeystorePassword());
            configProps.put("ssl.keystore.password", kafkaProperties.getKeystorePassword());
            configProps.put("ssl.keystore.location", kafkaProperties.getKeystoreLocation());
            configProps.put("ssl.endpoint.identification.algorithm", "");
        }

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new StringDeserializer()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            KafkaTemplate<String, String> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Afin d'éviter que l'appli s'arrête (crash total) lorsque Kafka tombe
        factory.setMissingTopicsFatal(false);

        LOGGER.info("Récupération des kafka properties pour le GUKafkaConsumerConfig...");
        int backOffInterval = kafkaProperties.getBackoffInterval();
        int backOffMaxAttempts = kafkaProperties.getBackoffMaxAttempts();

        BackOff bo = new FixedBackOff(backOffInterval, backOffMaxAttempts);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), bo));

        return factory;
    }
}
