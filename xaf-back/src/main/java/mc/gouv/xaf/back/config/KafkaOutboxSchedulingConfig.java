package mc.gouv.xaf.back.config;

import mc.gouv.xaf.back.properties.DemPropertyNotFoundException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.GouvSchedulerService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.impl.KafkaOutboxSchedulingJobImpl;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

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

    public static final String XAF_KAFKA_OUTBOX_SCHEDULING_CRONEXPRESSION = "XAF_KAFKA_OUTBOX_SCHEDULING_CRONEXPRESSION";
    public static final String XAF_KAFKA_OUTBOX_RETRY_NB = "XAF_KAFKA_OUTBOX_RETRY_NB";
    private Integer retryNb = null;
    public static final String XAF_KAFKA_OUTBOX_RETRY_INTERVAL = "XAF_KAFKA_OUTBOX_RETRY_INTERVAL";
    private Integer retryInterval = null;

    @Autowired
    private GouvSchedulerService schedulerService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @PostConstruct
    private void init() throws SchedulerException, DemPropertyNotFoundException {
        LOGGER.info("Récupération en base des propriétés nécessaires à la configuration de l'Outbox Kafka...");
        String cronExpression = retrieveCronExpression();
        retryNb = retrieveRetryNb();
        retryInterval = retrieveRetryInterval();

        LOGGER.info("Paramétrage du Job Quartz pour traitement de l'Outbox Kafka...");
        JobDetail jobDetail = schedulerService.buildJobDetail(KafkaOutboxSchedulingJobImpl.class, "KafkaOutboxSchedulingJob");
        Trigger trigger = schedulerService.buildJobTrigger(jobDetail, "KafkaOutboxSchedulingTrigger", cronExpression);
        schedulerService.startOrUpdateScheduledJob(jobDetail, trigger);
    }

    private String retrieveCronExpression() throws DemPropertyNotFoundException {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_KAFKA_OUTBOX_SCHEDULING_CRONEXPRESSION);
        if (propertiesDTO == null || StringUtils.isBlank(propertiesDTO.getValue())) {
            throw new DemPropertyNotFoundException(XAF_KAFKA_OUTBOX_SCHEDULING_CRONEXPRESSION);
        }
        return propertiesDTO.getValue();
    }

    private Integer retrieveRetryNb() throws DemPropertyNotFoundException {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_KAFKA_OUTBOX_RETRY_NB);
        if (propertiesDTO == null) {
            throw new DemPropertyNotFoundException(XAF_KAFKA_OUTBOX_RETRY_NB);
        }
        return Integer.parseInt(propertiesDTO.getValue());
    }

    private Integer retrieveRetryInterval() throws DemPropertyNotFoundException {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_KAFKA_OUTBOX_RETRY_INTERVAL);
        if (propertiesDTO == null) {
            throw new DemPropertyNotFoundException(XAF_KAFKA_OUTBOX_RETRY_INTERVAL);
        }
        return Integer.parseInt(propertiesDTO.getValue());
    }

    public Integer getRetryNb() {
        return retryNb;
    }

    public Integer getRetryInterval() {
        return retryInterval;
    }

}
