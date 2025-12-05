package mc.gouv.xaf.back.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class KafkaProperties {

    @Value("${mc.gouv.appli.shared.backapi.kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Value("${mc.gouv.busmsg.kafka.server.name}")
    private String bootstrapServersConfig;

    @Value("${mc.gouv.appli.shared.backapi.kafka.ssl.enabled:false}")
    private boolean kafkaSSLEnabled;

    @Value("${mc.gouv.appli.shared.backapi.kafka.ssl.truststore.location}")
    private String truststoreLocation;

    @Value("${mc.gouv.appli.shared.backapi.kafka.ssl.truststore.password}")
    private String truststorePassword;

    @Value("${mc.gouv.appli.shared.backapi.kafka.ssl.keystore.location}")
    private String keystoreLocation;

    @Value("${mc.gouv.appli.shared.backapi.kafka.ssl.keystore.password}")
    private String keystorePassword;

    @Value("${mc.gouv.appli.shared.backapi.kafka.producer.maxrequestsizeconfig:20971520}")
    private String maxRequestSizeConfig;

    @Value("${mc.gouv.appli.shared.backapi.kafka.consumer.fetchmaxbytes:20971520}")
    private String fetchMaxBytes;

    @Value("${mc.gouv.appli.shared.backapi.consumer.maxpartitionfetchbytes:20971520}")
    private String maxPartitionFetchBytes;

    @Value("${mc.gouv.appli.shared.backapi.kafka.outboxschedulingcron:* * * ? * *}")
    private String outboxSchedulingCron;

    @Value("${mc.gouv.appli.shared.backapi.kafka.outboxretry:3}")
    private Integer outboxRetry;

    @Value("${mc.gouv.appli.shared.backapi.kafka.outboxretryinterval:15}")
    private Integer outboxRetryInterval;

    @Value("${mc.gouv.appli.shared.backapi.kafka.consumer.backoffinterval:100}")
    private Integer backoffInterval;

    @Value("${mc.gouv.appli.shared.backapi.kafka.consumer.backoffmaxattempts:3}")
    private Integer backoffMaxAttempts;

    @Value("${mc.gouv.appli.shared.backapi.kafka.consumer.jobtimeout:10}")
    private Integer dltConsumerJobTimeout;

}
