package mc.gouv.xaf.back.service.itg.gichuni.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.config.BackserverCondition;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;

/**
 * 
 * Ce Consumer consomme les messages de la DLT du topic gu-to-ts-{codeAppli} afin de les y remettre.
 * L'API pourra donc à nouveau les consommer sur gu-to-ts-{codeAppli}.
 * Le KafkaListener de ce Consumer n'est actif que sur demande via un Job lancé depuis le BO.
 * L'API n'a pas accès à ce code.
 * 
 * @author qdeme
 *
 */
@Service
@Conditional(BackserverCondition.class)
public class GUKafkaDLTConsumer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaDLTConsumer.class);
	
	private Map<Integer,Integer> initialEndOffsetsPerPartition = null;
	private Map<Integer,Integer> currentOffsetsPerPartition = null;
	private Map<Integer,Integer> nbMessagesTraitesParPartition = null;
	
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
	 * KafkaListener du DLT du topic gu-to-ts-{codeAppli} (gu-to-ts-${codeAppli}.DLT)
	 * Jamais actif sauf sur activation via Job depuis la page des Jobs du BO.
	 * Il sert à copier les messages sur le topic initial afin que l'API les traite une nouvelle fois.
	 * Les messages sont acknowledged sur le DLT.
	 * 
	 * @param consumerRecord
	 * @param header
	 * @param consumer
	 */
	@KafkaListener(id = "gu-to-ts-consumer-dlt", topics = "gu-to-ts-${application.name}.DLT", groupId = "${application.name}", autoStartup = "false")
	public void dltListen(ConsumerRecord<String, Object> consumerRecord,
			@Headers Map<String, String> header, Consumer<?, ?> consumer) {
		
		LOGGER.info("Message reçu sur le DLT (" + consumerRecord.topic() + "," + consumerRecord.partition() + "," + consumerRecord.offset()
		+ "," + consumerRecord.key() + ") : " + consumerRecord.value());
		
		if (initialEndOffsetsPerPartition == null) {
			initialEndOffsetsPerPartition = getLogEndOffsets(consumer);
			for (Integer partition : initialEndOffsetsPerPartition.keySet()) {
				nbMessagesTraitesParPartition.put(partition, 0);
			}
			LOGGER.info("Stockage des logEndOffsets initiaux par partition : " + initialEndOffsetsPerPartition);
		}
		
		LOGGER.info("Current offset : " + consumerRecord.offset() + ", initialLogEndOffset : " + initialEndOffsetsPerPartition.get(consumerRecord.partition()));
		
		String topicInitial = "gu-to-ts-" + gouvPropertiesResolver.getDemarcheId().toLowerCase();
		LOGGER.info("Remise du message sur le topic initial (" + topicInitial + "), sur la même partition (" + consumerRecord.partition() + ") et avec la même clé (" + consumerRecord.key() + ")...");
		kafkaTemplate.send(topicInitial, consumerRecord.partition(), consumerRecord.key(), consumerRecord.value().toString());
		
		// Mise à jour du nombre de messages traités par partition
		nbMessagesTraitesParPartition.put(consumerRecord.partition(), nbMessagesTraitesParPartition.get(consumerRecord.partition())+1);
		LOGGER.info("Fin de la remise du message.");
		
		currentOffsetsPerPartition.put(consumerRecord.partition(), (int)consumerRecord.offset());
		
		// Ne pas processer plus de messages que ceux présents initialement dans le topic au moment du lancement du Job
		if (hasEverythingBeenRead()) {
			LOGGER.info("logEndOffset atteint sur toutes les partitions, arrêt du du KafkaListener gu-to-ts-consumer-dlt...");
			kafkaListenerEndpointRegistry.getListenerContainer("gu-to-ts-consumer-dlt").stop();
			
			jobOn = false;
		}
		
	}
	
	private boolean hasEverythingBeenRead() {
		for (Integer partition : initialEndOffsetsPerPartition.keySet()) {
			Integer partitionInitialEndOffset = initialEndOffsetsPerPartition.get(partition);
			Integer partitionCurrentOffset = currentOffsetsPerPartition.get(partition);
			if (partitionCurrentOffset != null && (partitionCurrentOffset < (partitionInitialEndOffset-1))) {
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private Map<Integer,Integer> getLogEndOffsets(Consumer<?, ?> consumer) {
		List<KafkaMetric> me = (List<KafkaMetric>) consumer.metrics().values().stream().filter(m -> "records-lead".equals(m.metricName().name())).collect(Collectors.toList());
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
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
		LOGGER.info("================ GHKafkaDLTConsumer.traiterDLT()");
		
		LOGGER.info("Démarrage du KafkaListener gu-to-ts-consumer-dlt puis attente jusqu'à fin de la redirection des messages...");
		
		Integer timeout = guKafkaUtils.getDltConsumerJobTimeout();
		
		String msg = "";
		try {
		
			initialEndOffsetsPerPartition = null;
			currentOffsetsPerPartition = new HashMap<Integer,Integer>();
			nbMessagesTraitesParPartition = new HashMap<Integer,Integer>();
			jobOn = true;
			kafkaListenerEndpointRegistry.getListenerContainer("gu-to-ts-consumer-dlt").start();
			
			Integer nbSleep = 0;
			while (jobOn) {
				Thread.sleep(1000);
				nbSleep++;
				
				if (nbSleep >= timeout && initialEndOffsetsPerPartition == null) {
					LOGGER.info("Déjà " + timeout + " secondes écoulées et initialEndOffsetsPerPartition non initialisé, il n'y a donc aucun message à traiter dans le DLT.");
					jobOn = false;
				}
			}
		
		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'exécution du Job GHKafkaDLTConsumer.traiterDLT()", e);
			msg = e.getMessage();
		}
		
		LOGGER.info("jobOn = false, le Job est terminé, renvoi d'un message textuel pour DemandeJobServiceImpl...");
		String topicInitial = "gu-to-ts-" + gouvPropertiesResolver.getDemarcheId().toLowerCase();
		
		Integer nbMessagesTraites = 0;
		String partitionDetails = "";
		for (Integer partition : nbMessagesTraitesParPartition.keySet()) {
			Integer nb = nbMessagesTraitesParPartition.get(partition);
			if (nb > 0) {
				nbMessagesTraites += nb;
				if (partitionDetails.length() > 0) {
					partitionDetails += ", ";
				}
				partitionDetails += nb + " sur p" + partition;
			}
		}
		if (partitionDetails.length() > 0) {
			partitionDetails = "(" + partitionDetails + ")";
		}
		
		if (nbMessagesTraites <= 1) {
			msg = nbMessagesTraites + " message du Dead Letter Topic a été remis sur le topic initial " + topicInitial + " " + partitionDetails + ".";
		}
		else {
			msg = nbMessagesTraites + " messages du Dead Letter Topic ont été remis sur le topic initial " + topicInitial + " " + partitionDetails + ".";
		}
		LOGGER.info("================ Fin GHKafkaDLTConsumer.traiterDLT()");
		
		return msg;
	}
	
}
