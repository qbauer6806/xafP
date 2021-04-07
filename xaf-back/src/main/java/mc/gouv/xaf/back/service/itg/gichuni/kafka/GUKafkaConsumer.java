package mc.gouv.xaf.back.service.itg.gichuni.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.config.ApiserverCondition;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.GUGenericKafkaMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.exception.GUKafkaException;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;

/**
 * 
 * Consumer de messages Kafka provenant du Guichet Unique (topic gu-to-ts-{codeAppli})
 * 
 * @author qdeme
 *
 */
@Service
@Conditional(ApiserverCondition.class)
public class GUKafkaConsumer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaConsumer.class);
	
	@Autowired
	private GUKafkaUtils guKafkaUtils;
	
	@Autowired
	private GUKafkaEventListener guKafkaEventListener;
	
	private static ObjectMapper mapper = new ObjectMapper();

	@KafkaListener(id = "gu-to-ts-consumer", topics = "gu-to-ts-${application.name}", groupId = "${application.name}")
	public void listen(ConsumerRecord<String, Object> consumerRecord) {

		LOGGER.info("Message reçu de Kafka (GU) (" + consumerRecord.topic() + "," + consumerRecord.partition() + "," + consumerRecord.offset()
		+ "," + consumerRecord.key() + ") : " + consumerRecord.value());
		
		String messageStr = (String)consumerRecord.value();
	    
	    GUGenericKafkaMessage genericMessage = null;
		try {
			genericMessage = mapper.readValue(messageStr, GUGenericKafkaMessage.class);
		    if (!guKafkaUtils.isMessageVersionSupported(genericMessage.getVersion())) {
		    	throw new GUKafkaException("Version de message Kafka (GU) non supportée (" + genericMessage.getVersion() + ")");
		    }
		} catch (JsonProcessingException e) {
			LOGGER.error("Erreur lors du mapper.readValue() du message Kafka (GU) reçu", e);
		}

		
	    // Dispatcher le message dans la démarche au bon endroit
	    if ("desinscription-usager-gu".equals(genericMessage.getType())) {
	    	if ("v1".equals(genericMessage.getVersion())) {
				try {
					mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DesinscriptionUsagerGUMessage message =
							mapper.readValue(messageStr, mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DesinscriptionUsagerGUMessage.class);
					guKafkaEventListener.desinscriptionUsagerGuichetUnique(message);
				} catch (JsonProcessingException e) {
					LOGGER.error("Erreur lors du second mapper.readValue() du message Kafka (GU) reçu", e);
				}
	    	}
	    }
	    
	    
	}
	
}
