package mc.gouv.xaf.back.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

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

    public String getKafkaEnabled() {
        return kafkaEnabled;
    }

    public void setKafkaEnabled(String kafkaEnabled) {
        this.kafkaEnabled = kafkaEnabled;
    }

    public String getBootstrapServersConfig() {
        return bootstrapServersConfig;
    }

    public void setBootstrapServersConfig(String bootstrapServersConfig) {
        this.bootstrapServersConfig = bootstrapServersConfig;
    }

    public String getKafkaSSLEnabled() {
        return kafkaSSLEnabled;
    }

    public void setKafkaSSLEnabled(String kafkaSSLEnabled) {
        this.kafkaSSLEnabled = kafkaSSLEnabled;
    }

    public String getTruststoreLocation() {
        return truststoreLocation;
    }

    public void setTruststoreLocation(String truststoreLocation) {
        this.truststoreLocation = truststoreLocation;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public String getKeystoreLocation() {
        return keystoreLocation;
    }

    public void setKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getMaxRequestSizeConfig() {
        return maxRequestSizeConfig;
    }

    public void setMaxRequestSizeConfig(String maxRequestSizeConfig) {
        this.maxRequestSizeConfig = maxRequestSizeConfig;
    }

    public String getFetchMaxBytes() {
        return fetchMaxBytes;
    }

    public void setFetchMaxBytes(String fetchMaxBytes) {
        this.fetchMaxBytes = fetchMaxBytes;
    }

    public String getMaxPartitionFetchBytes() {
        return maxPartitionFetchBytes;
    }

    public void setMaxPartitionFetchBytes(String maxPartitionFetchBytes) {
        this.maxPartitionFetchBytes = maxPartitionFetchBytes;
    }
}
