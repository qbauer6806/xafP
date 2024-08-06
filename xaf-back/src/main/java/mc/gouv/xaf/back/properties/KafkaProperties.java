package mc.gouv.xaf.back.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class KafkaProperties {

    @Value("${mc.gouv.${application.name}.shared.backapi.kafka.enabled:false}")
    private String kafkaEnabled;

    @Value("${mc.gouv.busmsg.kafka.server.name}")
    private String bootstrapServersConfig;

    @Value("${mc.gouv.${application.name}.shared.backapi.kafka.ssl.enabled:false}")
    private String kafkaSSLEnabled;

    @Value("${mc.gouv.${application.name}.shared.backapi.kafka.ssl.truststore.location}")
    private String truststoreLocation;

    @Value("${mc.gouv.${application.name}.shared.backapi.kafka.ssl.truststore.password}")
    private String truststorePassword;

    @Value("${mc.gouv.${application.name}.shared.backapi.kafka.ssl.keystore.location}")
    private String keystoreLocation;

    @Value("${mc.gouv.${application.name}.shared.backapi.kafka.ssl.keystore.password}")
    private String keystorePassword;

    @Value("${mc.gouv.${application.name}.shared.backapi.kafka.producer.maxrequestsizeconfig:20971520}")
    private String maxRequestSizeConfig;

    @Value("${mc.gouv.${application.name}.shared.backapi.kafka.consumer.fetchmaxbytes:20971520}")
    private String fetchMaxBytes;

    @Value("${mc.gouv.${application.name}.shared.backapi.consumer.maxpartitionfetchbytes:20971520}")
    private String maxPartitionFetchBytes;

}
