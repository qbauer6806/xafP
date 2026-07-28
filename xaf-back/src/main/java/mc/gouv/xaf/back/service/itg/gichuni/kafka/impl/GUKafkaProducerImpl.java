package mc.gouv.xaf.back.service.itg.gichuni.kafka.impl;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.ChangementStatutDemandeMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.CreationAccesTSMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.CreationDemandeMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DesinscriptionUsagerTSMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.GUKafkaMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.SuppressionDemandeMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.SuppressionPaiementMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.SynchronisationDemandesMessage;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.UsagerDemandesRecapDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

/**
 * Service permettant la production de messages pour le Guichet Unique via Kafka
 *
 * @author qdeme
 */
@Service
@ConditionalOnExpression(value = "'${mc.gouv.appli.shared.backapi.kafka.enabled}' == 'true'")
@RequiredArgsConstructor
public class GUKafkaProducerImpl implements GUKafkaProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaProducerImpl.class);

    private final GouvPropertiesResolver gouvPropertiesResolver;
    private final KafkaOutboxService guKafkaOutboxService;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void sendCreationDemandeMessage(Integer usagerId, Integer demandeId, String identifiant, Date dateCreation,
            RecapDemandesDTO recapDemandes) {
        if (DemarchesUtils.isUsagerCourrier(usagerId)) {
            LOGGER.info(
                    "sendCreationDemandeMessage - L'usager est un usager courrier, aucun message à envoyer au Guichet Unique...");
            return;
        }
        LOGGER.info(
                "sendCreationDemandeMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        CreationDemandeMessage cdm = new CreationDemandeMessage(gouvPropertiesResolver.getDemarcheId(),
                usagerId.toString(), demandeId, identifiant, dateCreation, StatutSimplifieEnum.EN_COURS.name(),
                recapDemandes);
        sendToOutbox(cdm, cdm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
    }

    @Override
    public void sendChangementStatutDemandeMessage(Integer usagerId, Integer demandeId, String identifiant,
            StatutSimplifieEnum statutSimplifie, Date dateStatutSimplifie, RecapDemandesDTO recapDemandes) {
        if (DemarchesUtils.isUsagerCourrier(usagerId)) {
            LOGGER.info(
                    "sendChangementStatutDemandeMessage - L'usager est un usager courrier, aucun message à envoyer au Guichet Unique...");
            return;
        }
        LOGGER.info(
                "sendChangementStatutDemandeMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        ChangementStatutDemandeMessage cdm = new ChangementStatutDemandeMessage(gouvPropertiesResolver.getDemarcheId(),
                usagerId.toString(), demandeId, identifiant, dateStatutSimplifie, statutSimplifie.name(),
                recapDemandes);
        sendToOutbox(cdm, cdm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
    }

    @Override
    public void sendSuppressionDemandeMessage(Integer usagerId, Integer demandeId, String identifiant,
            Date dateSuppression, RecapDemandesDTO recapDemandes) {
        if (DemarchesUtils.isUsagerCourrier(usagerId)) {
            LOGGER.info(
                    "sendSuppressionDemandeMessage - L'usager est un usager courrier, aucun message à envoyer au Guichet Unique...");
            return;
        }
        LOGGER.info(
                "sendSuppressionDemandeMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        SuppressionDemandeMessage cdm = new SuppressionDemandeMessage(gouvPropertiesResolver.getDemarcheId(),
                usagerId.toString(), demandeId, identifiant, dateSuppression, recapDemandes);
        sendToOutbox(cdm, cdm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
    }

    @Override
    public void sendDesinscriptionUsagerTSMessage(Integer usagerId) {
        LOGGER.info(
                "sendDesinscriptionUsagerTSMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        DesinscriptionUsagerTSMessage dutsm = new DesinscriptionUsagerTSMessage(gouvPropertiesResolver.getDemarcheId(),
                usagerId.toString());
        sendToOutbox(dutsm, dutsm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
    }

    @Override
    public void sendSynchronisationDemandesMessage(List<UsagerDemandesRecapDTO> usagerDemandesRecap) {
        LOGGER.info(
                "sendSynchronisationDemandesMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        SynchronisationDemandesMessage sdm = new SynchronisationDemandesMessage(demarcheId, usagerDemandesRecap);
        try {
            String json = mapper.writeValueAsString(sdm);
            LOGGER.info("Message à envoyer : {}", json);
        } catch (JacksonException e) {
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
        } catch (JacksonException e) {
            LOGGER.error("Erreur lors du mapper.writeValueAsString()", e);
        }
    }

    @Override
    public void sendCreationAccesTSMessage(Integer usagerId) {
        LOGGER.info(
                "sendCreationAccesTSMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        CreationAccesTSMessage catsm = new CreationAccesTSMessage(gouvPropertiesResolver.getDemarcheId(),
                usagerId.toString());
        sendToOutbox(catsm, catsm.getUsagerId(), GUKafkaUtils.GU_TO_TS_TOPIC);
    }

    @Override
    public void sendSuppressionPaiementMessage(String userLegacyId, String requestNumber) {
        LOGGER.info(
                "sendSuppressionPaiementMessage - Placement du message à envoyer au Guichet Unique dans l'Outbox Kafka...");
        SuppressionPaiementMessage spm = new SuppressionPaiementMessage(gouvPropertiesResolver.getDemarcheId(),
                userLegacyId, requestNumber);
        sendToOutbox(spm, userLegacyId, GUKafkaUtils.TS_TO_GU_PAYMENT_TOPIC);

    }

}
