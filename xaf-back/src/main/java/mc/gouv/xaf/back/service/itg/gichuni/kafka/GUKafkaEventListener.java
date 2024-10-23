package mc.gouv.xaf.back.service.itg.gichuni.kafka;

import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.GUKafkaMessage;

/**
 * Listener implémenté par la démarche, permettant de traiter les différents messages reçus du Guichet Unique via le
 * broker Kafka. Charge à la démarche de prendre en compte la version du message pour le traitement.
 *
 * @author qdeme
 */
public interface GUKafkaEventListener {

    void desinscriptionUsagerGuichetUnique(GUKafkaMessage message);

}
