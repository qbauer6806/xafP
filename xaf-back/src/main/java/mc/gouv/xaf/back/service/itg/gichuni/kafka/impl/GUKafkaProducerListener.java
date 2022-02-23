package mc.gouv.xaf.back.service.itg.gichuni.kafka.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.config.KafkaOutboxSchedulingConfig;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

/**
 * 
 * Classe ProducerListener permettant le traitement a posteriori d'un message Guichet Unique qui a pu
 * être remis à Kafka ou au contraire qui n'a pas pu être remis.
 * 
 * @author qdeme
 *
 */
@Component
public class GUKafkaProducerListener implements ProducerListener<String, String> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaProducerListener.class);
	
    @Autowired
    private KafkaOutboxService kafkaOutboxService;
    
    @Autowired
    private PropertiesService propertiesService;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private MailService mailService;
    
    @Autowired
    private AfBackUtils afBackUtils;
    
    private static String XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE = "XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE";
    
    private static String MAIL_TEMPLATE_KAFKA_DLT_CORPS = "MAIL_TEMPLATE_KAFKA_DLT_CORPS";
    
    private static String MAIL_TEMPLATE_KAFKA_DLT_OBJET = "MAIL_TEMPLATE_KAFKA_DLT_OBJET";

	@Override
	public void onSuccess(ProducerRecord<String, String> producerRecord, RecordMetadata recordMetadata) {
		Integer pkKafkaOutbox = getPkKafkaOutboxFromProducerRecord(producerRecord);
		if (producerRecord.topic().endsWith(".DLT")) {
			LOGGER.info("Message envoyé avec succès sur le DLT " + producerRecord.topic() + " (key=" + producerRecord.key() + ", partition=" + producerRecord.partition() + ", value=" + producerRecord.value() + ")");
			
			LOGGER.info("Envoi d'un e-mail à la liste du support technique...");
			PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE);
	        if (propertiesDTO.getValue() != null) {
	            String[] adresses = propertiesDTO.getValue().trim().split(",");
	            // Composition du mail
	            Map<String, Object> model = new HashMap<>();
	            model.put("topic", producerRecord.topic());
	            model.put("key", producerRecord.key());
	            model.put("partition", producerRecord.partition());
	            model.put("value", producerRecord.value());
	            model.put("demarcheId", gouvPropertiesResolver.getDemarcheId());
	            EmailInfoDTO emailInfoDTO = createMailRollbackES(producerRecord);
	            for (String adresseMail : adresses) {
	                emailInfoDTO.addTo(adresseMail, "Support Technique");
	            }
	            try {
					mailService.sendMail(emailInfoDTO, model);
				} catch (Exception e) {
					LOGGER.error("Erreur lors de l'envoi de l'e-mail au support technique suite à envoi d'un message Kafka sur le DLT");
				}
	        }
			
		}
		else if (pkKafkaOutbox == null) {
			LOGGER.error("Message envoyé avec succès mais pkKafkaOutbox null ! Situation anormale, impossible de supprimer le message de l'outbox");
		}
		else {
			LOGGER.info("Message envoyé avec succès (pkKafkaOutbox " + pkKafkaOutbox + ")");
			LOGGER.info("Suppression du message de l'Outbox Kafka...");
			kafkaOutboxService.deleteOutboxElement(pkKafkaOutbox);
		}
	}
	
    private EmailInfoDTO createMailRollbackES(ProducerRecord<String, String> producerRecord) {
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(MAIL_TEMPLATE_KAFKA_DLT_CORPS);
        emailInfo.setSubjectTemplateCode(MAIL_TEMPLATE_KAFKA_DLT_OBJET);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos().getEmailReplytoNom());
        emailInfo.setLangue("fr");
        return emailInfo;
    }

	// TODO @Override
	public void onError(ProducerRecord<String, String> producerRecord, Exception exception) {
		Integer pkKafkaOutbox = getPkKafkaOutboxFromProducerRecord(producerRecord);
		if (pkKafkaOutbox == null) {
			LOGGER.error("Erreur lors de l'envoi du message dans Kafka et pkKafkaOutbox null ! Situation anormale, impossible de mettre à jour son statut dans l'Outbox pour un retry", exception);
		}
		else {
			LOGGER.error("Erreur lors de l'envoi du message dans Kafka (pkKafkaOutbox " + pkKafkaOutbox + ")", exception);
			LOGGER.error("Mise à jour du statut du message dans l'Outbox Kafka...");
			KafkaOutboxDTO dto = kafkaOutboxService.getOutboxElement(pkKafkaOutbox);
			dto.setStatut(KafkaOutboxSchedulingConfig.KAFKA_OUTBOX_STATUT_ECHEC);
			dto.setNbFailedAttempts(dto.getNbFailedAttempts()+1);
			if (dto.getDateLastAttempt() == null) {
				dto.setDateLastAttempt(new Date());
			}
			kafkaOutboxService.updateOutboxElement(dto);
		}
	}
	
	private Integer getPkKafkaOutboxFromProducerRecord(ProducerRecord<String, String> producerRecord) {
		Iterable<Header> iterable = producerRecord.headers().headers(KafkaOutboxSchedulingConfig.PK_KAFKA_OUTBOX);
		if (iterable == null) {
			return null;
		}
		Iterator<Header> it = iterable.iterator();
		if (!it.hasNext()) {
			return null;
		}
		return Integer.parseInt(new String(it.next().value()));
		
	}

}
