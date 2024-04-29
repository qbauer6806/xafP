package mc.gouv.xaf.back.service.itg.gichuni.kafka.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.*;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Service permettant la production de messages pour le Guichet Unique via Kafka
 *
 * @author qdeme
 */
@Service
@ConditionalOnExpression(value = "'${mc.gouv.${application.name}.shared.backapi.kafka.enabled}' == 'true'")
public class GUKafkaProducerImpl implements GUKafkaProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaProducerImpl.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private KafkaOutboxService guKafkaOutboxService;


    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void sendCreationDemandeMessage(Integer usagerId, Integer demandeId, String identifiant, Date dateCreation, RecapDemandesDTO recapDemandes) {
        if (DemarchesUtils.isUsagerCourrier(usagerId)) {
            LOGGER.info("sendCreationDemandeMessage - L'usager est un usager courrier, aucun message à envoyer au Guichet Unique...");
            return;
        }
        LOGGER.info("sendCreationDemandeMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        CreationDemandeMessage cdm = new CreationDemandeMessage(gouvPropertiesResolver.getDemarcheId(), usagerId.toString(), demandeId, identifiant,
                dateCreation, StatutSimplifieEnum.EN_COURS.name(), recapDemandes);
        sendToOutbox(cdm, cdm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
    }

    @Override
    public void sendChangementStatutDemandeMessage(Integer usagerId, Integer demandeId, String identifiant, StatutSimplifieEnum statutSimplifie, Date dateStatutSimplifie, RecapDemandesDTO recapDemandes) {
        if (DemarchesUtils.isUsagerCourrier(usagerId)) {
            LOGGER.info("sendChangementStatutDemandeMessage - L'usager est un usager courrier, aucun message à envoyer au Guichet Unique...");
            return;
        }
        LOGGER.info("sendChangementStatutDemandeMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        ChangementStatutDemandeMessage cdm = new ChangementStatutDemandeMessage(gouvPropertiesResolver.getDemarcheId(), usagerId.toString(), demandeId, identifiant,
                dateStatutSimplifie, statutSimplifie.name(), recapDemandes);
        sendToOutbox(cdm, cdm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
    }

    @Override
    public void sendSuppressionDemandeMessage(Integer usagerId, Integer demandeId, String identifiant, Date dateSuppression, RecapDemandesDTO recapDemandes) {
        if (DemarchesUtils.isUsagerCourrier(usagerId)) {
            LOGGER.info("sendSuppressionDemandeMessage - L'usager est un usager courrier, aucun message à envoyer au Guichet Unique...");
            return;
        }
        LOGGER.info("sendSuppressionDemandeMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
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

    @Override
    public void sendSynchronisationDemandesMessage(List<UsagerDemandesRecapDTO> usagerDemandesRecap) {
        LOGGER.info("sendSynchronisationDemandesMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        SynchronisationDemandesMessage sdm = new SynchronisationDemandesMessage(demarcheId, usagerDemandesRecap);
        try {
            String json = mapper.writeValueAsString(sdm);
            LOGGER.info("Message à envoyer : {}", json);
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors du mapper.writeValueAsString(sdm)", e);
        }

        sendToOutbox(sdm, demarcheId, GUKafkaUtils.GU_TO_TS_TOPIC);
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

    @Override
    public void sendCreationAccesTSMessage(Integer usagerId) {
        LOGGER.info("sendCreationAccesTSMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        CreationAccesTSMessage catsm = new CreationAccesTSMessage(gouvPropertiesResolver.getDemarcheId(), usagerId.toString());
        sendToOutbox(catsm, catsm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
    }

}
