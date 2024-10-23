package mc.gouv.xaf.back.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.core.KafkaTemplate;

import mc.gouv.xaf.back.config.KafkaOutboxSchedulingConfig;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;

/**
 * Job Quartz permettant la lecture périodique de l'Outbox Kafka afin d'envoyer les messages vers Kafka
 *
 * @author qdeme
 */
@DisallowConcurrentExecution
@ConditionalOnExpression(value = "'${mc.gouv.${application.name}.shared.backapi.kafka.enabled}' == 'true'")
public class KafkaOutboxSchedulingJobImpl implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOutboxSchedulingJobImpl.class);

    @Autowired
    private KafkaOutboxService kafkaOutboxService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaOutboxSchedulingConfig kafkaOutboxSchedulingConfig;

    @Override
    public synchronized void execute(JobExecutionContext jobExecutionContext) {
        LOGGER.debug("KafkaOutboxSchedulingJob.execute({})", jobExecutionContext);
        List<KafkaOutboxDTO> outbox = kafkaOutboxService.getOutboxElements();
        for (KafkaOutboxDTO obx : outbox) {
            if (KafkaOutboxSchedulingConfig.KAFKA_OUTBOX_STATUT_NOUVEAU.equals(obx.getStatut()) || (
                    KafkaOutboxSchedulingConfig.KAFKA_OUTBOX_STATUT_ECHEC.equals(obx.getStatut()) && isRetryPossible(
                            obx))) {

                if (KafkaOutboxSchedulingConfig.KAFKA_OUTBOX_STATUT_ECHEC.equals(obx.getStatut())) {
                    LOGGER.info(
                            "KafkaOutboxSchedulingJob.execute() a trouvé un message à RENVOYER à Kafka ({}, tentative #{})",
                            obx, (obx.getNbFailedAttempts() + 1));
                } else {
                    LOGGER.info("KafkaOutboxSchedulingJob.execute() a trouvé un message à envoyer à Kafka ({})", obx);
                }
                LOGGER.info("Envoi dans Kafka (pkKafkaOutbox {})...", obx.getPkKafkaOutbox());
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(obx.getTopic(), obx.getKey(),
                        obx.getContenu());
                producerRecord.headers().add(new RecordHeader(KafkaOutboxSchedulingConfig.PK_KAFKA_OUTBOX,
                        obx.getPkKafkaOutbox().toString().getBytes()));
                obx.setStatut(KafkaOutboxSchedulingConfig.KAFKA_OUTBOX_STATUT_EN_COURS);
                obx.setDateLastAttempt(new Date());
                kafkaOutboxService.updateOutboxElement(obx);
                kafkaTemplate.send(producerRecord);
            }
        }
    }

    private boolean isRetryPossible(KafkaOutboxDTO obx) {
        if (obx.getNbFailedAttempts() < kafkaOutboxSchedulingConfig.getRetryNb()) {
            long secondsDiff = (new Date().getTime() - obx.getDateLastAttempt().getTime()) / 1000;
            return secondsDiff >= kafkaOutboxSchedulingConfig.getRetryInterval();
        }
        return false;
    }
}
