package mc.gouv.xaf.back.service.itg.gichuni.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
 * Consumer de messages Kafka provenant du Guichet Unique (topic gichuni-to-ts-{codeAppli})
 * 
 * @author qdeme
 *
 */
@Service
@Conditional({ ApiserverCondition.class})
@ConditionalOnExpression(value = "'${mc.gouv.${application.name}.shared.backapi.kafka.enabled}' == 'true'")
public class GUKafkaConsumer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaConsumer.class);
	
	@Autowired
	private GUKafkaUtils guKafkaUtils;
	
	@Autowired
	private GUKafkaEventListener guKafkaEventListener;
	
	private static final ObjectMapper mapper = new ObjectMapper();

	@KafkaListener(id = "gichuni-to-ts-consumer", topics = "gichuni-to-ts-${application.name}", groupId = "${application.name}")
	public void listen(ConsumerRecord<String, Object> consumerRecord) {

		String topic = consumerRecord.topic();
		String key = consumerRecord.key();
		Object value = consumerRecord.value();
		LOGGER.info("Message reçu de Kafka (GU) ({},{},{},{}) : {}", topic, consumerRecord.partition(),
				consumerRecord.offset(), key, value);
		
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
		if (genericMessage == null) {
			LOGGER.warn("Attention, genericMessage null !");
			return;
		}
	    if ("desinscription-usager-gichuni".equals(genericMessage.getType()) && "v1".equals(genericMessage.getVersion())) {
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
