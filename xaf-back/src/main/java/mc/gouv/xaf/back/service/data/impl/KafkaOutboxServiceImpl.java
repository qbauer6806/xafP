package mc.gouv.xaf.back.service.data.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.config.KafkaOutboxSchedulingConfig;
import mc.gouv.xaf.back.data.dao.KafkaOutboxRepository;
import mc.gouv.xaf.back.data.entity.KafkaOutboxBO;
import mc.gouv.xaf.back.data.transformer.KafkaOutboxTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;

/**
 * 
 * Service permettant la manipulation de l'outbox direction Kafka
 *
 * @author qdeme
 * 
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class KafkaOutboxServiceImpl implements KafkaOutboxService {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOutboxServiceImpl.class);

	@Autowired
	private KafkaOutboxRepository kafkaOutboxRepository;
	
	@Override
	public List<KafkaOutboxDTO> getOutboxElements() {
		// Log DEBUG pour le GET, car exécuté périodiquement et trop souvent, cela polluerait les logs
		LOGGER.debug("Récupération en base des éléments Outbox Kafka...");
		List<KafkaOutboxBO> bos = kafkaOutboxRepository.findAll();
		LOGGER.debug("Transformation bo -> dto ...");
		return KafkaOutboxTransformer.bo2Dto(bos);
	}

	@Override
	public KafkaOutboxDTO createOutboxElement(KafkaOutboxDTO outboxElement) {
		LOGGER.debug("Sauvegarde de l'élément Outbox Kafka...");
		outboxElement.setDateCreation(new Date());
		outboxElement.setNbFailedAttempts(0);
		outboxElement.setStatut(KafkaOutboxSchedulingConfig.KAFKA_OUTBOX_STATUT_NOUVEAU);
		KafkaOutboxBO bo = KafkaOutboxTransformer.dto2Bo(outboxElement);
		bo = kafkaOutboxRepository.save(bo);
		LOGGER.debug("Transformation bo -> dto ...");
		return KafkaOutboxTransformer.bo2Dto(bo);
	}

	@Override
	public KafkaOutboxDTO updateOutboxElement(KafkaOutboxDTO outboxElement) {
		LOGGER.debug("Récupération de l'élément Outbox Kafka à mettre à jour...");
		Optional<KafkaOutboxBO> boOpt = kafkaOutboxRepository.findById(outboxElement.getPkKafkaOutbox());
		if (!boOpt.isPresent()) {
			throw new DemarchesServiceException("Élément Outbox Kafka introuvable", HttpStatus.NOT_FOUND);
		}
		
		LOGGER.debug("Mise à jour de l'élément Outbox Kafka...");
		
		KafkaOutboxBO bo = boOpt.get();
		bo.setContenu(outboxElement.getContenu());
		bo.setDateCreation(outboxElement.getDateCreation());
		bo.setDateLastAttempt(outboxElement.getDateLastAttempt());
		bo.setKey(outboxElement.getKey());
		bo.setNbFailedAttempts(outboxElement.getNbFailedAttempts());
		bo.setStatut(outboxElement.getStatut());
		bo.setTopic(outboxElement.getTopic());
		bo = kafkaOutboxRepository.save(bo);
		
		LOGGER.debug("Transformation bo -> dto ...");
		return KafkaOutboxTransformer.bo2Dto(bo);
	}

	@Override
	public void deleteOutboxElement(Integer pkGUKafkaOutbox) {
		LOGGER.debug("Récupération de l'élément Outbox Kafka à supprimer...");
		Optional<KafkaOutboxBO> boOpt = kafkaOutboxRepository.findById(pkGUKafkaOutbox);
		if (!boOpt.isPresent()) {
			throw new DemarchesServiceException("Élément Outbox Kafka introuvable", HttpStatus.NOT_FOUND);
		}
		LOGGER.debug("Suppression...");
		kafkaOutboxRepository.delete(boOpt.get());
	}

	@Override
	public KafkaOutboxDTO getOutboxElement(Integer pkOutboxElement) {
		LOGGER.debug("Récupération de l'élément Outbox Kafka de pkOutboxElement=" + pkOutboxElement + "...");
		Optional<KafkaOutboxBO> bo = kafkaOutboxRepository.findById(pkOutboxElement);
		if (!bo.isPresent()) {
			return null;
		}
		LOGGER.debug("Transformation bo -> dto ...");
		return KafkaOutboxTransformer.bo2Dto(bo.get());
		
	}

	@Override
	public Integer resetAllOutboxElements() {
		LOGGER.info("KafkaOutboxServiceImpl.resetAllOutboxElements() - Récupération en base des éléments Outbox Kafka...");
		List<KafkaOutboxBO> bos = kafkaOutboxRepository.findAll();
		if (bos != null && bos.size() > 0) {
			for (KafkaOutboxBO bo : bos ) {
				bo.setDateLastAttempt(null);
				bo.setNbFailedAttempts(0);
				bo.setStatut(KafkaOutboxSchedulingConfig.KAFKA_OUTBOX_STATUT_NOUVEAU);
			}
			LOGGER.info("Sauvegarde en base des éléments Outbox Kafka modifiés...");
			kafkaOutboxRepository.saveAll(bos);
			return bos.size();
		}
		LOGGER.info("Aucun élément Outbox Kafka à rejouer");
		return 0;
	}

}
