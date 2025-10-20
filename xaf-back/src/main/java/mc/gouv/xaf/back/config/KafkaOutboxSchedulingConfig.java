package mc.gouv.xaf.back.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import mc.gouv.xaf.back.properties.KafkaProperties;
import mc.gouv.xaf.back.service.GouvSchedulerService;
import mc.gouv.xaf.back.service.impl.KafkaOutboxSchedulingJobImpl;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

/**
 * Paramétrage du Job Quartz permettant le traitement de l'Outbox Kafka
 *
 * @author qdeme
 */
@Service
@ConditionalOnExpression(value = "'${mc.gouv.${application.name}.shared.backapi.kafka.enabled}' == 'true'")
public class KafkaOutboxSchedulingConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOutboxSchedulingConfig.class);

    public static final String KAFKA_OUTBOX_STATUT_NOUVEAU = "NOUVEAU";
    public static final String KAFKA_OUTBOX_STATUT_ECHEC = "ECHEC";
    public static final String KAFKA_OUTBOX_STATUT_EN_COURS = "EN_COURS";

    public static final String PK_KAFKA_OUTBOX = "pkKafkaOutbox";

    @Getter
    private Integer retryNb = null;
    @Getter
    private Integer retryInterval = null;

    @Autowired
    private GouvSchedulerService schedulerService;

    @Autowired
    private KafkaProperties kafkaProperties;

    @PostConstruct
    private void init() throws SchedulerException {
        LOGGER.info("Récupération en base des propriétés nécessaires à la configuration de l'Outbox Kafka...");
        String cronExpression = kafkaProperties.getOutboxSchedulingCron();
        retryNb = kafkaProperties.getOutboxRetry();
        retryInterval = kafkaProperties.getOutboxRetryInterval();

        LOGGER.info("Paramétrage du Job Quartz pour traitement de l'Outbox Kafka...");
        JobDetail jobDetail = schedulerService.buildJobDetail(KafkaOutboxSchedulingJobImpl.class,
                "KafkaOutboxSchedulingJob");
        Trigger trigger = schedulerService.buildJobTrigger(jobDetail, "KafkaOutboxSchedulingTrigger", cronExpression);
        schedulerService.startOrUpdateScheduledJob(jobDetail, trigger);
    }

}
