package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;

import java.util.List;

/**
 * Service permettant la manipulation de l'outbox direction Kafka
 *
 * @author qdeme
 */
public interface KafkaOutboxService {

    List<KafkaOutboxDTO> getOutboxElements();

    KafkaOutboxDTO getOutboxElement(Integer pkOutboxElement);

    KafkaOutboxDTO createOutboxElement(KafkaOutboxDTO outboxElement);

    KafkaOutboxDTO updateOutboxElement(KafkaOutboxDTO outboxElement);

    void deleteOutboxElement(Integer pkGUKafkaOutbox);

    Integer resetAllOutboxElements();

    Integer getNbOutboxElements();

}
