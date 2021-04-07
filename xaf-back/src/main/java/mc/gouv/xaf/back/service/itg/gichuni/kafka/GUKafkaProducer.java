package mc.gouv.xaf.back.service.itg.gichuni.kafka;

import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.CreationDemandeMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DesinscriptionUsagerTSMessage;

/**
 * 
 * Service permettant la production de messages pour le Guichet Unique via Kafka
 * 
 * @author qdeme
 *
 */
public interface GUKafkaProducer {

	void sendCreationDemandeMessage(CreationDemandeMessage message);

	void sendDesinscriptionUsagerTSMessage(DesinscriptionUsagerTSMessage message);

}
