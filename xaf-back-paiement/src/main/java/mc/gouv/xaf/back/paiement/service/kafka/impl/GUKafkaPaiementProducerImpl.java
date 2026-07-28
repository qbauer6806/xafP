package mc.gouv.xaf.back.paiement.service.kafka.impl;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.paiement.service.kafka.GUKafkaPaiementProducer;
import mc.gouv.xaf.back.paiement.service.kafka.dto.AffichagePaiementMessage;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.GUKafkaMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Service
@ConditionalOnExpression(value = "'${mc.gouv.appli.shared.backapi.kafka.enabled}' == 'true'")
@RequiredArgsConstructor
public class GUKafkaPaiementProducerImpl implements GUKafkaPaiementProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaPaiementProducerImpl.class);

    private final KafkaOutboxService guKafkaOutboxService;

    private static final ObjectMapper mapper = JsonMapper.builder().build();

    @Override
    public void sendAffichagePaiementMessage(AffichagePaiementMessage apm) {
        LOGGER.info(
                "sendAffichagePaiementMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        sendToOutbox(apm, apm.getUserLegacyId(), GUKafkaUtils.TS_TO_GU_PAYMENT_TOPIC);
    }


    private void sendToOutbox(GUKafkaMessage message, String key, String topic) {
        KafkaOutboxDTO dto = new KafkaOutboxDTO();
        try {
            dto.setContenu(mapper.writeValueAsString(message));
            dto.setKey(key);
            dto.setTopic(topic);
            dto = guKafkaOutboxService.createOutboxElement(dto);

            LOGGER.info("Élément Outbox créé : {}", dto);
        } catch (JacksonException e) {
            LOGGER.error("Erreur lors du mapper.writeValueAsString()", e);
        }
    }
}
