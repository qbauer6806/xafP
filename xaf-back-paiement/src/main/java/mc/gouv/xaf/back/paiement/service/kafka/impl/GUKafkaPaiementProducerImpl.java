package mc.gouv.xaf.back.paiement.service.kafka.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mc.gouv.xaf.back.paiement.service.kafka.GUKafkaPaiementProducer;
import mc.gouv.xaf.back.paiement.service.kafka.dto.PaymentTypeEnum;
import mc.gouv.xaf.back.paiement.service.kafka.dto.AffichagePaiementMessage;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.GUKafkaMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@ConditionalOnExpression(value = "'${mc.gouv.${application.name}.shared.backapi.kafka.enabled}' == 'true'")
public class GUKafkaPaiementProducerImpl implements GUKafkaPaiementProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaPaiementProducerImpl.class);

    @Autowired
    private KafkaOutboxService guKafkaOutboxService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void sendAffichagePaiementMessage(String userLegacyId,
            PaymentTypeEnum paymentType, String paymentMethodToken, LocalDateTime paymentDate, double paymentAmount,
            String paymentStatus, String requestObject, String requestNumber, LocalDateTime requestDate, String paymentMethodExpiryDate, String paymentMethodAccount, String effectiveBrand, String link) {
        LOGGER.info(
                "sendAffichagePaiementMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        AffichagePaiementMessage apm = new AffichagePaiementMessage(gouvPropertiesResolver.getDemarcheId(),
                userLegacyId, paymentType, paymentMethodToken, paymentDate, paymentAmount, paymentStatus, requestObject,
                requestNumber, requestDate, paymentMethodExpiryDate, paymentMethodAccount, effectiveBrand, link);
        sendToOutbox(apm, userLegacyId, GUKafkaUtils.TS_TO_GU_PAYMENT_TOPIC);
    }


    private void sendToOutbox(GUKafkaMessage message, String key, String topic) {
        KafkaOutboxDTO dto = new KafkaOutboxDTO();
        try {
            dto.setContenu(mapper.writeValueAsString(message));
            dto.setKey(key);
            dto.setTopic(topic);
            dto = guKafkaOutboxService.createOutboxElement(dto);

            LOGGER.info("Élément Outbox créé : {}", dto);
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors du mapper.writeValueAsString()", e);
        }
    }
}
