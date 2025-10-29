package mc.gouv.xaf.back.properties;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class GouvPropertiesResolverImpl implements GouvPropertiesResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvPropertiesResolverImpl.class);

    @Value("${application.name}")
    private String applicationName;

    @Value("${application.module}")
    private String applicationModule;

    @Value("${display.name}")
    private String demarcheId;

    // GLOBAL PROPERTIES
    @Value("${mc.gouv.logon.url:}")
    private String logonUrl;

    @Value("${mc.gouv.logon.api.url:}")
    private String logonRestServerUrl;

    @Value("${mc.gouv.file.api.url}")
    private String fileUrl;

    @Value("${mc.gouv.mail.api.url}")
    private String mailUrl;

    @Value("${mc.gouv.sms.api.url:}")
    private String smsUrl;

    @Getter
    @Value("${mc.gouv.nomen.api.url}")
    private String nomenUrl;

    @Value("${mc.gouv.vscan.api.url}")
    private String vscanUrl;

    @Getter
    @Value("${mc.gouv.vscan.activated}")
    private boolean vscanActivated;

    @Value("${mc.gouv.gichkey.url}")
    private String gichkeyUrl;

    @Value("${mc.gouv.gichuni.api.url}")
    private String gichuniUrl;

    // SHARED PROPERTIES
    @Value("${mc.gouv.${application.name}.shared.backapi.file.containerId}")
    private String containerId;

    @Value("${mc.gouv.backapi.usagerscache.duration}")
    private String usagersCacheDuration;

    // Valeur par défaut : 24h
    @Value("${mc.gouv.backapi.utilisateurscache.duration:86400000}")
    private String utilisateursCacheDuration;

    @Value("${mc.gouv.payscache.duration}")
    private String paysCacheDuration;

    @Value("${mc.gouv.${application.name}.shared.backapi.vscan.jwt}")
    private String vscanJwt;

    @Value("${mc.gouv.${application.name}.shared.backapi.mail.jwt}")
    private String mailJwt;

    @Value("${mc.gouv.${application.name}.shared.backapi.file.jwt}")
    private String fileJwt;

    @Value("${mc.gouv.${application.name}.shared.backapi.sms.jwt:}")
    private String smsJwt;

    @Getter
    @Value("${mc.gouv.shared.backapi.nomen.jwt}")
    private String nomenJwt;

    @Value("${mc.gouv.${application.name}.shared.backapi.paiement.enabled:false}")
    private boolean paiementEnabled;

    @Value("${mc.gouv.shared.backapi.rest.pagesize:250}")
    private String usagersPageSize;

    @Value("${mc.gouv.${application.name}.shared.backapi.back.url}")
    private String backUrl;

    @Value("${mc.gouv.${application.name}.shared.backapi.front.url}")
    private String frontUrl;

    @Value("${mc.gouv.${application.name}.2tiers.bo.url:}")
    private String _2tiersBoUrl;

    @Value("${mc.gouv.${application.name}.2tiers.bo.jwt:}")
    private String _2tiersBoJwt;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Value("${mc.gouv.file.extensions.whitelist}")
    private String extensionsWhitelist;

    @Value("${mc.gouv.${application.name}.shared.backapi.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${mc.gouv.mwpaymt.api.url:}")
    private String mwpaymtUrl;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private ArchivageProperties archivageProperties;

    @Autowired
    private UlisProperties ulisProperties;

    @Override
    public String getContainerId() {
        return containerId;
    }

    @Override
    public String getDemarcheId() {
        return demarcheId;
    }

    @Override
    public String getFileUrl() {
        return fileUrl;
    }

    @Override
    public String getVScanUrl() {
        return vscanUrl;
    }

    @Override
    public String getVscanJwt() {
        return vscanJwt;
    }

    @Override
    public String getMailUrl() {
        return mailUrl;
    }

    @Override
    public String getSmsUrl() {
        return smsUrl;
    }

    @Override
    public String getFileJwt() {
        return fileJwt;
    }

    @Override
    public String getMailJwt() {
        return mailJwt;
    }

    @Override
    public String getSmsJwt() {
        return smsJwt;
    }

    @Override
    public String getFrontUrl() {
        return frontUrl;
    }

    @Override
    public String getBackUrl() {
        return backUrl;
    }

    @Override
    public String getGouvSharedLogonUrl() {
        return logonUrl;
    }

    @Override
    public String getGouvSharedLogonRestUrl() {
        return logonRestServerUrl;
    }

    @Override
    public long getUsagersCacheDuration() {
        return Long.parseLong(usagersCacheDuration);
    }

    @Override
    public long getUtilisateursCacheDuration() {
        return Long.parseLong(utilisateursCacheDuration);
    }

    @Override
    public long getPaysCacheDuration() {
        return Long.parseLong(paysCacheDuration);
    }

    @Override
    public Integer getUsagersPageSize() {
        return Integer.parseInt(usagersPageSize);
    }

    @Override
    public boolean isApiserver() {
        return "apiserver".equals(applicationModule);
    }

    @Override
    public boolean isBackserver() {
        return "backserver".equals(applicationModule);
    }

    @Override
    public String getGUKafkaBootstrapServersConfig() {
        return kafkaProperties.getBootstrapServersConfig();
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public boolean getGUKafkaSSLEnabled() {
        return kafkaProperties.isKafkaSSLEnabled();
    }

    @Override
    public String getGUKafkaSSLTrustStoreLocation() {
        return kafkaProperties.getTruststoreLocation();
    }

    @Override
    public String getGUKafkaSSLTrustStorePassword() {
        return kafkaProperties.getTruststorePassword();
    }

    @Override
    public String getGUKafkaSSLKeyStoreLocation() {
        return kafkaProperties.getKeystoreLocation();
    }

    @Override
    public String getGUKafkaSSLKeyStorePassword() {
        return kafkaProperties.getKeystorePassword();
    }

    @Override
    public boolean getKafkaEnabled() {
        return kafkaProperties.isKafkaEnabled();
    }

    @Override
    public String getGichkeyUrl() {
        return gichkeyUrl;
    }

    @Override
    public String getGichuniUrl() {
        return gichuniUrl;
    }

    @Override
    public String getGUKafkaProducerMaxRequestSize() {

        return kafkaProperties.getMaxRequestSizeConfig();
    }

    @Override
    public String getGUKafkaConsumerFetchMaxBytes() {

        return kafkaProperties.getFetchMaxBytes();
    }

    @Override
    public String getGUKafkaConsumerMaxPartitionFetchBytes() {

        return kafkaProperties.getMaxPartitionFetchBytes();
    }

    @Override
    public String getApiRioUrl() {
        return archivageProperties.getRioUrl();
    }

    @Override
    public String getApiRioJwt() {
        return archivageProperties.getRioJwt();
    }

    @Override
    public String getApiRioCodeAppli() {
        return archivageProperties.getRioCodeAppli();
    }

    @Override
    public String getApiRioCodeNotice() {
        return archivageProperties.getRioCodeNotice();
    }

    @Override
    public boolean isPaiementEnabled() {
        return paiementEnabled;
    }

    public String getPorteDocUrl() {
        String value = getGichuniUrl();
        return StringUtils.isBlank(value) ? "vide" : value + "/public/doc-holder";
    }

    @Override
    public String getApiUlisMoyensGenerauxUrl() {
        return ulisProperties.getApiUlisMoyensGenerauxUrl();
    }

    @Override
    public String getApiUlisTiersOrganisationUrl() {
        return ulisProperties.getApiUlisTiersOrganisationUrl();
    }

    @Override
    public String getApiUlisAuthenticationUser() {
        return ulisProperties.getApiUlisAuthentUser();
    }

    @Override
    public String getApiUlisAuthenticationPassword() {
        return ulisProperties.getApiUlisAuthentPassword();
    }

    @Override
    public String getApiUlisFunctionalUser() {
        return ulisProperties.getUlisFunctionalAccount();
    }

    @Override
    public String getApiUlisWorkflowUrl() {
        return ulisProperties.getUlisUrlWorkflow();
    }

    @Override
    public String getApiUlisCommercialisationUrl() {
        return ulisProperties.getUlisUrlCommercialisation();
    }

    @Override
    public String get2TiersBoUrl() {
        return _2tiersBoUrl;
    }

    @Override
    public String get2TiersBoJwt() {
        return _2tiersBoJwt;
    }

    @Override
    public String getMaxFileSize() {
        return maxFileSize;
    }

    @Override
    public String getExtensionsWhitelist() {
        return extensionsWhitelist;
    }

    @Override
    public boolean getSmsEnabled() {
        return smsEnabled;
    }

    @Override
    public String getMwpaymtUrl() {
        return mwpaymtUrl;
    }

}
