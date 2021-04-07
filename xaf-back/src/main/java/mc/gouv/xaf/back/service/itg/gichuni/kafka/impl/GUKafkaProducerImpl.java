package mc.gouv.xaf.back.service.itg.gichuni.kafka.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.CreationDemandeMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DesinscriptionUsagerTSMessage;
import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;

/**
 * 
 * Service permettant la production de messages pour le Guichet Unique via Kafka
 * 
 * @author qdeme
 *
 */
@Service
public class GUKafkaProducerImpl implements GUKafkaProducer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaProducer.class);
	
	@Autowired
	private KafkaOutboxService guKafkaOutboxService;
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public void sendCreationDemandeMessage(CreationDemandeMessage message) {
		LOGGER.info("sendCreationDemandeMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
		KafkaOutboxDTO dto = new KafkaOutboxDTO();
		try {
			dto.setContenu(mapper.writeValueAsString(message));
			dto.setKey(message.getUsagerId());
			dto.setTopic("ts-to-gu");
			dto = guKafkaOutboxService.createOutboxElement(dto);
			
			LOGGER.info("Élément Outbox créé : " + dto);
		} catch (JsonProcessingException e) {
			LOGGER.error("Erreur lors du mapper.writeValueAsString()", e);
		}
	}
	
	@Override
	public void sendDesinscriptionUsagerTSMessage(DesinscriptionUsagerTSMessage message) {
		LOGGER.info("sendDesinscriptionUsagerTSMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
		KafkaOutboxDTO dto = new KafkaOutboxDTO();
		try {
			dto.setContenu(mapper.writeValueAsString(message));
			dto.setKey(message.getUsagerId());
			dto.setTopic("ts-to-gu");
			dto = guKafkaOutboxService.createOutboxElement(dto);
			
			LOGGER.info("Élément Outbox créé : " + dto);
		} catch (JsonProcessingException e) {
			LOGGER.error("Erreur lors du mapper.writeValueAsString()", e);
		}
	}
	
}
