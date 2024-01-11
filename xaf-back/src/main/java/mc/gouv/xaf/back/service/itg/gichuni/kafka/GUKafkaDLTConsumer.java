package mc.gouv.xaf.back.service.itg.gichuni.kafka;

import mc.gouv.xaf.back.config.BackserverCondition;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ce Consumer consomme les messages de la DLT du topic gichuni-to-ts-{codeAppli} afin de les y remettre.
 * L'API pourra donc à nouveau les consommer sur gichuni-to-ts-{codeAppli}.
 * Le KafkaListener de ce Consumer n'est actif que sur demande via un Job lancé depuis le BO.
 * L'API n'a pas accès à ce code.
 *
 * @author qdeme
 */
@Service
@Conditional({BackserverCondition.class})
@ConditionalOnExpression(value = "'${mc.gouv.${application.name}.backapi.kafka.enabled}' == 'true'")
public class GUKafkaDLTConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaDLTConsumer.class);

    private Map<Integer, Integer> initialEndOffsetsPerPartition = null;
    private Map<Integer, Integer> currentOffsetsPerPartition = null;
    private Map<Integer, Integer> nbMessagesTraitesParPartition = null;

    private boolean jobOn = false;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private GUKafkaUtils guKafkaUtils;

    /**
     * KafkaListener du DLT du topic gichuni-to-ts-{codeAppli} (gichuni-to-ts-${codeAppli}.DLT)<br>
     * Jamais actif sauf sur activation via Job depuis la page des Jobs du BO.<br>
     * Il sert à copier les messages sur le topic initial afin que l'API les traite une nouvelle fois.<br>
     * Les messages sont acknowledged sur le DLT.
     */
    @KafkaListener(id = "gichuni-to-ts-consumer-dlt", topics = "gichuni-to-ts-${application.name}.DLT", groupId = "${application.name}", autoStartup = "false")
    public void dltListen(ConsumerRecord<String, Object> consumerRecord,
                          @Headers Map<String, String> header, Consumer<?, ?> consumer) {

        String topic = consumerRecord.topic();
        String key = consumerRecord.key();
        Object value = consumerRecord.value();
        LOGGER.info("Message reçu sur le DLT ({}, {}, {}, {}) : {}", topic, consumerRecord.partition(), consumerRecord.offset(), key, value);

        if (initialEndOffsetsPerPartition == null) {
            initialEndOffsetsPerPartition = getLogEndOffsets(consumer);
            for (Integer partition : initialEndOffsetsPerPartition.keySet()) {
                nbMessagesTraitesParPartition.put(partition, 0);
            }
            LOGGER.info("Stockage des logEndOffsets initiaux par partition : {}", initialEndOffsetsPerPartition);
        }

        LOGGER.info("Current offset : {}, initialLogEndOffset : {}", consumerRecord.offset(), initialEndOffsetsPerPartition.get(consumerRecord.partition()));

        String topicInitial = "gichuni-to-ts-" + gouvPropertiesResolver.getApplicationName();
        LOGGER.info("Remise du message sur le topic initial ({}), sur la même partition ({}) et avec la même clé ({})...", topicInitial, consumerRecord.partition(), key);
        kafkaTemplate.send(topicInitial, consumerRecord.partition(), key, value.toString());

        // Mise à jour du nombre de messages traités par partition
        nbMessagesTraitesParPartition.put(consumerRecord.partition(), nbMessagesTraitesParPartition.get(consumerRecord.partition()) + 1);
        LOGGER.info("Fin de la remise du message.");

        currentOffsetsPerPartition.put(consumerRecord.partition(), (int) consumerRecord.offset());

        // Ne pas processer plus de messages que ceux présents initialement dans le topic au moment du lancement du Job
        if (hasEverythingBeenRead()) {
            LOGGER.info("logEndOffset atteint sur toutes les partitions, arrêt du du KafkaListener gichuni-to-ts-consumer-dlt...");
            MessageListenerContainer listenerContainer =
                    kafkaListenerEndpointRegistry.getListenerContainer("gichuni-to-ts-consumer-dlt");
            if (listenerContainer != null) {
                listenerContainer.stop();
            }
            jobOn = false;
        }
    }

    private boolean hasEverythingBeenRead() {
        for (Map.Entry<Integer, Integer> entry : initialEndOffsetsPerPartition.entrySet()) {
            Integer partitionCurrentOffset = currentOffsetsPerPartition.get(entry.getKey());
            if (partitionCurrentOffset != null && (partitionCurrentOffset < (entry.getValue() - 1))) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> getLogEndOffsets(Consumer<?, ?> consumer) {
        List<KafkaMetric> me = (List<KafkaMetric>) consumer.metrics().values().stream().filter(m -> "records-lead".equals(m.metricName().name())).collect(Collectors.toList());
        Map<Integer, Integer> map = new HashMap<>();
        for (KafkaMetric km : me) {
            for (String key : km.metricName().tags().keySet()) {
                if ("partition".equals(key)) {
                    map.put(Integer.parseInt(km.metricName().tags().get(key)), Integer.parseInt(km.metricValue().toString().replace(".0", "")));
                }
            }
        }
        return map;
    }

    public String traiterDLT() {
        LOGGER.info("================ GUKafkaDLTConsumer.traiterDLT()");

        LOGGER.info("Démarrage du KafkaListener gichuni-to-ts-consumer-dlt puis attente jusqu'à fin de la redirection des messages...");

        Integer timeout = guKafkaUtils.getDltConsumerJobTimeout();

        try {
            initialEndOffsetsPerPartition = null;
            currentOffsetsPerPartition = new HashMap<>();
            nbMessagesTraitesParPartition = new HashMap<>();
            jobOn = true;
            kafkaListenerEndpointRegistry.getListenerContainer("gichuni-to-ts-consumer-dlt").start();

            int nbSleep = 0;
            while (jobOn) {
                Thread.sleep(1000);
                nbSleep++;

                if (nbSleep >= timeout && initialEndOffsetsPerPartition == null) {
                    LOGGER.info("Déjà {} secondes écoulées et initialEndOffsetsPerPartition non initialisé, il n'y a donc aucun message à traiter dans le DLT.", timeout);
                    jobOn = false;
                }
            }

        } catch (InterruptedException e) {
            LOGGER.error("Erreur lors de l'exécution du Job GUKafkaDLTConsumer.traiterDLT()", e);
            Thread.currentThread().interrupt();
        }

        LOGGER.info("jobOn = false, le Job est terminé, renvoi d'un message textuel pour DemandeJobServiceImpl...");
        String topicInitial = "gichuni-to-ts-" + gouvPropertiesResolver.getDemarcheId().toLowerCase();

        int nbMessagesTraites = 0;
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<Integer, Integer> entry : nbMessagesTraitesParPartition.entrySet()) {
            Integer nb = entry.getValue();
            if (nb > 0) {
                nbMessagesTraites += nb;
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(nb).append(" sur p").append(entry.getKey());
            }
        }

        if (builder.length() > 0) {
            builder.insert(0, '(');
            builder.append(')');
        }

        String msg;
        if (nbMessagesTraites <= 1) {
            msg = nbMessagesTraites + " message du Dead Letter Topic a été remis sur le topic initial " + topicInitial + " " + builder + ".";
        } else {
            msg = nbMessagesTraites + " messages du Dead Letter Topic ont été remis sur le topic initial " + topicInitial + " " + builder + ".";
        }
        LOGGER.info("================ Fin GUKafkaDLTConsumer.traiterDLT()");

        return msg;
    }

}
