package mc.gouv.xaf.back.service.itg.gichuni.kafka.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.config.KafkaEnabledCondition;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.ChangementStatutDemandeMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.CreationDemandeMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DesinscriptionUsagerTSMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.GUKafkaMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.StatutSimplifieEnum;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.SuppressionDemandeMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;

/**
 * 
 * Service permettant la production de messages pour le Guichet Unique via Kafka
 * 
 * @author qdeme
 *
 */
@Service
@Conditional(KafkaEnabledCondition.class)
public class GUKafkaProducerImpl implements GUKafkaProducer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaProducer.class);
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;
	
	@Autowired
	private KafkaOutboxService guKafkaOutboxService;
	
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public void sendCreationDemandeMessage(Integer usagerId, Integer demandeId, String identifiant, Date dateCreation, RecapDemandesDTO recapDemandes) {
		
		LOGGER.info("sendCreationDemandeMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
		CreationDemandeMessage cdm = new CreationDemandeMessage(gouvPropertiesResolver.getDemarcheId(), usagerId.toString(), demandeId, identifiant,
				dateCreation, StatutSimplifieEnum.EN_COURS.name(), recapDemandes);
		sendToOutbox(cdm, cdm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
	}
	
	@Override
	public void sendChangementStatutDemandeMessage(Integer usagerId, Integer demandeId, String identifiant, StatutSimplifieEnum statutSimplifie, Date dateStatutSimplifie, RecapDemandesDTO recapDemandes) {
		LOGGER.info("sendCreationDemandeMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
		ChangementStatutDemandeMessage cdm = new ChangementStatutDemandeMessage(gouvPropertiesResolver.getDemarcheId(), usagerId.toString(), demandeId, identifiant,
				dateStatutSimplifie, statutSimplifie.name(), recapDemandes);
		sendToOutbox(cdm, cdm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
	}
	
	@Override
	public void sendSuppressionDemandeMessage(Integer usagerId, Integer demandeId, String identifiant, Date dateSuppression, RecapDemandesDTO recapDemandes) {
		LOGGER.info("sendCreationDemandeMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
		SuppressionDemandeMessage cdm = new SuppressionDemandeMessage(gouvPropertiesResolver.getDemarcheId(), usagerId.toString(), demandeId, identifiant,
				dateSuppression, recapDemandes);
		sendToOutbox(cdm, cdm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
	}
	
	@Override
	public void sendDesinscriptionUsagerTSMessage(Integer usagerId) {
		LOGGER.info("sendDesinscriptionUsagerTSMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
		DesinscriptionUsagerTSMessage dutsm = new DesinscriptionUsagerTSMessage(gouvPropertiesResolver.getDemarcheId(), usagerId.toString());
		sendToOutbox(dutsm, dutsm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
	}
	
	private void sendToOutbox(GUKafkaMessage message, String key, String topic) {
		KafkaOutboxDTO dto = new KafkaOutboxDTO();
		try {
			dto.setContenu(mapper.writeValueAsString(message));
			dto.setKey(key);
			dto.setTopic(topic);
			dto = guKafkaOutboxService.createOutboxElement(dto);
			
			LOGGER.info("Élément Outbox créé : " + dto);
		} catch (JsonProcessingException e) {
			LOGGER.error("Erreur lors du mapper.writeValueAsString()", e);
		}
	}
	
}
