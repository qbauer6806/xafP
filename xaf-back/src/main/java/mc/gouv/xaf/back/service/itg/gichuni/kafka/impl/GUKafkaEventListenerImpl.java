package mc.gouv.xaf.back.service.itg.gichuni.kafka.impl;

import mc.gouv.xaf.back.service.AfApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaEventListener;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DesinscriptionUsagerGUMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.GUKafkaMessage;

/**
 * Listener d'événements Kafka en provenance du Guichet Unique.
 *
 * @author qdeme
 */
@Component
public class GUKafkaEventListenerImpl implements GUKafkaEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaEventListenerImpl.class);

    @Autowired
    private AfApiService afApiService;

    @Override
    public void desinscriptionUsagerGuichetUnique(GUKafkaMessage message) {
        LOGGER.info("GUKafkaEventListenerImpl.desinscriptionUsagerGuichetUnique({})", message);

        if ("v1".equals(message.getVersion())) {
            DesinscriptionUsagerGUMessage desin = (DesinscriptionUsagerGUMessage) message;
            LOGGER.info("L'usager {} s'est désinscrit du Guichet Unique. Lancement de la désinscription dans le TS...",
                    desin.getUsagerId());
            // TODO quelle langue ? Non fournie par le FO... Présente uniquement pour chaque demande effectuée.
            afApiService.desinscriptionUsager(Integer.parseInt(desin.getUsagerId()), "fr", true);
        }
    }

}
