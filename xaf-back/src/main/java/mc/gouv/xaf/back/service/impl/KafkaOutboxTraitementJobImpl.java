package mc.gouv.xaf.back.service.impl;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.KafkaOutboxTraitementJob;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe permettant l'exécution du job de traitement de l'Outbox Kafka depuis le BO
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class KafkaOutboxTraitementJobImpl implements KafkaOutboxTraitementJob {

    private final KafkaOutboxService kafkaOutboxService;

    @Override
    public String execute() {
        Integer nbElemsReset = kafkaOutboxService.resetAllOutboxElements();
        if (nbElemsReset == 0) {
            return "Aucun élément Outbox Kafka disponible en base pour traitement";
        }
        return nbElemsReset + " éléments Outbox Kafka ont été remis à zéro pour être envoyés à nouveau";
    }

}
