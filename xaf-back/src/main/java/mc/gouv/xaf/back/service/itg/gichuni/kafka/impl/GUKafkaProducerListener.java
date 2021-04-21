package mc.gouv.xaf.back.service.itg.gichuni.kafka.impl;

import java.util.Iterator;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.config.KafkaOutboxSchedulingConfig;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;

/**
 * 
 * Classe ProducerListener permettant le traitement a posteriori d'un message Guichet Unique qui a pu
 * être remis à Kafka ou au contraire qui n'a pas pu être remis.
 * 
 * @author qdeme
 *
 */
@Component
public class GUKafkaProducerListener implements ProducerListener<String, String> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaProducerListener.class);
	
    @Autowired
    private KafkaOutboxService kafkaOutboxService;

	@Override
	public void onSuccess(ProducerRecord<String, String> producerRecord, RecordMetadata recordMetadata) {
		Integer pkKafkaOutbox = getPkKafkaOutboxFromProducerRecord(producerRecord);
		if (producerRecord.topic().endsWith(".DLT")) {
			LOGGER.info("Message envoyé avec succès sur le DLT " + producerRecord.topic() + " (key=" + producerRecord.key() + ", partition=" + producerRecord.partition() + ")");
		}
		else if (pkKafkaOutbox == null) {
			LOGGER.error("Message envoyé avec succès mais pkKafkaOutbox null ! Situation anormale, impossible de supprimer le message de l'outbox");
		}
		else {
			LOGGER.info("Message envoyé avec succès (pkKafkaOutbox " + pkKafkaOutbox + ")");
			LOGGER.info("Suppression du message de l'Outbox Kafka...");
			kafkaOutboxService.deleteOutboxElement(pkKafkaOutbox);
		}
	}

	@Override
	public void onError(ProducerRecord<String, String> producerRecord, Exception exception) {
		Integer pkKafkaOutbox = getPkKafkaOutboxFromProducerRecord(producerRecord);
		if (pkKafkaOutbox == null) {
			LOGGER.error("Erreur lors de l'envoi du message dans Kafka et pkKafkaOutbox null ! Situation anormale, impossible de mettre à jour son statut dans l'Outbox pour un retry", exception);
		}
		else {
			LOGGER.error("Erreur lors de l'envoi du message dans Kafka (pkKafkaOutbox " + pkKafkaOutbox + ")", exception);
			LOGGER.error("Mise à jour du statut du message dans l'Outbox Kafka...");
			KafkaOutboxDTO dto = kafkaOutboxService.getOutboxElement(pkKafkaOutbox);
			dto.setStatut(KafkaOutboxSchedulingConfig.KAFKA_OUTBOX_STATUT_ECHEC);
			dto.setNbFailedAttempts(dto.getNbFailedAttempts()+1);
			kafkaOutboxService.updateOutboxElement(dto);
		}
	}
	
	private Integer getPkKafkaOutboxFromProducerRecord(ProducerRecord<String, String> producerRecord) {
		Iterable<Header> iterable = producerRecord.headers().headers(KafkaOutboxSchedulingConfig.PK_KAFKA_OUTBOX);
		if (iterable == null) {
			return null;
		}
		Iterator<Header> it = iterable.iterator();
		if (!it.hasNext()) {
			return null;
		}
		return Integer.parseInt(new String(it.next().value()));
		
	}

}
