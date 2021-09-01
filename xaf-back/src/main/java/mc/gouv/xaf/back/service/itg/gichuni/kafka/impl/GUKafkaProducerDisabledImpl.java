package mc.gouv.xaf.back.service.itg.gichuni.kafka.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.config.KafkaDisabledCondition;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.StatutSimplifieEnum;

/**
 * 
 * Service permettant la production de messages pour le Guichet Unique via Kafka
 * 
 * @author qdeme
 *
 */
@Service
@Conditional(KafkaDisabledCondition.class)
public class GUKafkaProducerDisabledImpl implements GUKafkaProducer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaProducer.class);
	
	@Override
	public void sendCreationDemandeMessage(Integer usagerId, Integer demandeId, String identifiant, Date dateCreation, RecapDemandesDTO recapDemandes) {
		LOGGER.info("sendCreationDemandeMessage - KafkaEnabled = false, aucun message à envoyer dans Kafka");
	}
	
	@Override
	public void sendChangementStatutDemandeMessage(Integer usagerId, Integer demandeId, String identifiant, StatutSimplifieEnum statutSimplifie, Date dateStatutSimplifie, RecapDemandesDTO recapDemandes) {
		LOGGER.info("sendCreationDemandeMessage - KafkaEnabled = false, aucun message à envoyer dans Kafka");
	}
	
	@Override
	public void sendSuppressionDemandeMessage(Integer usagerId, Integer demandeId, String identifiant, Date dateSuppression, RecapDemandesDTO recapDemandes) {
		LOGGER.info("sendCreationDemandeMessage - KafkaEnabled = false, aucun message à envoyer dans Kafka");
	}
	
	@Override
	public void sendDesinscriptionUsagerTSMessage(Integer usagerId) {
		LOGGER.info("sendCreationDemandeMessage - KafkaEnabled = false, aucun message à envoyer dans Kafka");
	}
	
}
