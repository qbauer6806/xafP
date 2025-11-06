package mc.gouv.xaf.back.service.itg.gichuni.kafka.config;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.impl.GUKafkaProducerListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration du Producer Kafka pour le Guichet Unique
 *
 * @author qdeme
 */
@EnableKafka
@EnableAsync
@Configuration
@ConditionalOnExpression(value = "'${mc.gouv.appli.shared.backapi.kafka.enabled}' == 'true'")
public class GUKafkaProducerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaProducerConfig.class);

    @Bean
    public ProducerFactory<String, String> producerFactory(GouvPropertiesResolver gouvPropertiesResolver) {
        LOGGER.info("Création du GUKafkaProducer...");
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                gouvPropertiesResolver.getGUKafkaBootstrapServersConfig());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Messages jusqu'à 20MB (rajouter aussi message.max.bytes=20971520 dans server.properties de Kafka sinon :
        // org.apache.kafka.common.errors.RecordTooLargeException: The request included a message larger than the max message size the server will accept.
        configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG,
                gouvPropertiesResolver.getGUKafkaProducerMaxRequestSize());

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

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(GUKafkaProducerListener guKafkaProducerListener,
            GouvPropertiesResolver gouvPropertiesResolver) {
        KafkaTemplate<String, String> kt = new KafkaTemplate<>(producerFactory(gouvPropertiesResolver));
        kt.setProducerListener(guKafkaProducerListener);
        return kt;
    }

    @Bean
    public KafkaOperations kafkaTemplateFlowable() {
        // utile pour éviter un conflit kafka avec flowable
        return new KafkaOperations() {

            @Override
            public CompletableFuture<SendResult> sendDefault(Object data) {
                return null;
            }

            @Override
            public CompletableFuture<SendResult> sendDefault(Object key, Object data) {
                return null;
            }

            @Override
            public CompletableFuture<SendResult> sendDefault(Integer partition, Object key, Object data) {
                return null;
            }

            @Override
            public CompletableFuture<SendResult> sendDefault(Integer partition, Long timestamp, Object key,
                    Object data) {
                return null;
            }

            @Override
            public CompletableFuture<SendResult> send(String topic, Object data) {
                return null;
            }

            @Override
            public CompletableFuture<SendResult> send(String topic, Object key, Object data) {
                return null;
            }

            @Override
            public CompletableFuture<SendResult> send(String topic, Integer partition, Object key, Object data) {
                return null;
            }

            @Override
            public CompletableFuture<SendResult> send(String topic, Integer partition, Long timestamp, Object key,
                    Object data) {
                return null;
            }

            @Override
            public CompletableFuture<SendResult> send(ProducerRecord record) {
                return null;
            }

            @Override
            public List<PartitionInfo> partitionsFor(String topic) {
                return List.of();
            }

            @Override
            public Map<MetricName, ? extends Metric> metrics() {
                return Map.of();
            }

            @Override
            public void flush() {
                // not needed
            }

            @Override
            public boolean isTransactional() {
                return false;
            }

            @Override
            public ConsumerRecord receive(String topic, int partition, long offset, Duration pollTimeout) {
                return null;
            }

            @Override
            public ConsumerRecords receive(Collection requested, Duration pollTimeout) {
                return null;
            }

            @Override
            public T executeInTransaction(OperationsCallback callback) {
                return null;
            }

            @Override
            public T execute(ProducerCallback callback) {
                return null;
            }

            @Override
            public CompletableFuture<SendResult> send(Message message) {
                return null;
            }
        };
    }
}
