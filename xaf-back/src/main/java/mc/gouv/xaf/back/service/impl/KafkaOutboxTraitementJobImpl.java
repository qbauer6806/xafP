package mc.gouv.xaf.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.service.KafkaOutboxTraitementJob;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;

/**
 * 
 * Classe permettant l'exécution du job de traitement de l'Outbox Kafka depuis le BO
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class KafkaOutboxTraitementJobImpl implements KafkaOutboxTraitementJob {

	@Autowired
	private KafkaOutboxService kafkaOutboxService;
	
	@Override
	public String execute() {
		Integer nbElemsReset = kafkaOutboxService.resetAllOutboxElements();
		if (nbElemsReset == 0) {
			return "Aucun élément Outbox Kafka disponible en base pour traitement";
		}
		return nbElemsReset + " éléments Outbox Kafka ont été remis à zéro pour être envoyés à nouveau";
	}

}
