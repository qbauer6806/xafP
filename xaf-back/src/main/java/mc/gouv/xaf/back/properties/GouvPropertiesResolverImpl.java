package mc.gouv.xaf.back.properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement.
 *
 * @author qdeme
 */
@Component
@Profile("gouv")
@Transactional(rollbackFor = Exception.class)
public class GouvPropertiesResolverImpl implements GouvPropertiesResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvPropertiesResolverImpl.class);

    @Value("${application.name}")
    private String applicationName;

    @Value("${application.module}")
    private String applicationModule;

    /**
     * Uppercase de application.name
     */
    private String demarcheId;

    private String applicationPrefix = StringUtils.EMPTY;


    ///// GLOBAL PROPERTIES
    @Value("${mc.gouv.logon.url:OPTIONAL}")
    private String logonUrl;

    @Value("${mc.gouv.logon.api.url:OPTIONAL}")
    private String logonRestServerUrl;

    @Value("${mc.gouv.file.api.url}")
    private String fileUrl;

    @Value("${mc.gouv.mail.api.url}")
    private String mailUrl;

    @Value("${mc.gouv.vscan.api.url}")
    private String vscanUrl;

    @Value("${mc.gouv.servicerest.api.pays.url}")
    private String paysRestUrl;

    @Value("${mc.gouv.gichkey.url}")
    private String gichkeyUrl;

    @Value("${mc.gouv.gichuni.api.url}")
    private String gichuniUrl;


    ///// SHARED PROPERTIES
    @Value("${mc.gouv.${application.name}.shared.backapi.file.containerId}")
    private String containerId;

    @Value("${mc.gouv.${application.name}.shared.backapi.activiti.processDefinitionKey}")
    private String processDefinitionKey;

    @Value("${mc.gouv.${application.name}.shared.backapi.usagerscache.duration}")
    private String usagersCacheDuration;

    @Value("${mc.gouv.${application.name}.shared.backapi.vscan.jwt}")
    private String vscanJwt;

    @Value("${mc.gouv.${application.name}.shared.backapi.mail.jwt}")
    private String mailJwt;

    @Value("${mc.gouv.${application.name}.shared.backapi.file.jwt}")
    private String fileJwt;

    @Value("${mc.gouv.${application.name}.shared.backapi.paiement.enabled:false}")
    private String paiementEnabled;

    @Value("${mc.gouv.${application.name}.shared.backapi.rest.pagesize:500}")
    private String usagersPageSize;

    @Value("${mc.gouv.${application.name}.shared.backapi.back.url}")
    private String backUrl;

    @Value("${mc.gouv.${application.name}.shared.backapi.front.url}")
    private String frontUrl;
    
    @Value("${mc.gouv.${application.name}.2tiers.bo.url:OPTIONAL}")
    private String _2tiersBoUrl;
    
    @Value("${mc.gouv.${application.name}.2tiers.bo.jwt:OPTIONAL}")
    private String _2tiersBoJwt;

    @Autowired
    private ElasticsearchProperties esProperties;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private ProxyProperties proxyProperties;

    @Autowired
    private ArchivageProperties archivageProperties;

    @Autowired
    private UlisProperties ulisProperties;

    /**
     * propertyEditor.getReadMethod() expose le getter, peut être null si on a une prorpriété en écriture seule
     */
    private Method getMethod(PropertyDescriptor propertyDescriptor) {
        Method method;
        try {
            LOGGER.info("Vérification de la propriété via le get : {}", propertyDescriptor.getReadMethod());
            method = propertyDescriptor.getReadMethod();
        } catch (SecurityException e) {
            LOGGER.error("Erreur lors de la récupération de la méthode");
            throw e;
        }
        return method;
    }

    private void checkProperties(List<String> propertiesNotFound, Method method, PropertyDescriptor propertyDescriptor)
            throws InvocationTargetException, IllegalAccessException {
        try {

            // Est-ce que l'indexation est activée ?
            boolean indexingEnabled = StringUtils.equals(esProperties.getIndexingEnabled(), "true");

            // Est-ce que le SSL est activé ?
            boolean sslEnabled = getGUKafkaSSLEnabled();

            // Est ce que l'archivage est activé ?
            boolean archivageEnabled = StringUtils.equals(archivageProperties.getArchivageEnabled(), "true");

            // On ignore la présence de la property si la méthode possède @GouvIndexationProperty mais que l'appli a
            // indexationEnabled=false
            boolean pasIgnorerIndexing = !(method
                    .getDeclaredAnnotation(GouvIndexationProperty.class) instanceof GouvIndexationProperty)
                    || (method.getDeclaredAnnotation(GouvIndexationProperty.class) instanceof GouvIndexationProperty
                    && indexingEnabled);

            // On ignore la présence de la property si la méthode possède @GouvSSLProperty mais que l'appli a
            // mc.gouv.af.back.external.gichuni.kafka.ssl.enabled=false
            boolean pasIgnorerSSL = !(method.getDeclaredAnnotation(GouvSSLProperty.class) instanceof GouvSSLProperty)
                    || (method.getDeclaredAnnotation(GouvSSLProperty.class) instanceof GouvSSLProperty && sslEnabled);

            // On ignore la présence de la property si la méthode possède @GouvArchivageProperty mais que l'appli a
            // archivage.enabled=false ou pas présente
            boolean pasIgnorerArchivage = !(method
                    .getDeclaredAnnotation(GouvArchivageProperty.class) instanceof GouvArchivageProperty)
                    || (method.getDeclaredAnnotation(GouvArchivageProperty.class) instanceof GouvArchivageProperty
                    && archivageEnabled);

            if (pasIgnorerIndexing && pasIgnorerSSL && pasIgnorerArchivage) {
                Object value = method.invoke(this);
                if (value instanceof String) {
                    if (StringUtils.isBlank((String) value)) {
                        propertiesNotFound.add(propertyDescriptor.getReadMethod().toString());
                    }
                } else if (value == null) {
                    propertiesNotFound.add(propertyDescriptor.getReadMethod().toString());
                }
            }
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Erreur lors de l'invocation de la méthode");
            throw e;
        }
    }

    @PostConstruct
    private void initPrefix() throws IntrospectionException, IllegalAccessException, InvocationTargetException,
            GouvPropertyNotFoundException {

        if (StringUtils.isNotBlank(applicationName)) {
            applicationPrefix = "." + applicationName;
            demarcheId = StringUtils.upperCase(applicationName);
        }

        // Vérification que chaque propriété a bien été configurée
        List<String> propertiesNotFound = new ArrayList<>();
        try {

            for (PropertyDescriptor propertyDescriptor : Introspector
                    .getBeanInfo(GouvPropertiesResolverImpl.class, Object.class).getPropertyDescriptors()) {

                Method method = getMethod(propertyDescriptor);

                checkProperties(propertiesNotFound, method, propertyDescriptor);
            }

        } catch (IntrospectionException e) {
            LOGGER.error("Erreur lors de l'introspection");
            throw e;
        }

        if (!propertiesNotFound.isEmpty()) {
            throw new GouvPropertyNotFoundException(propertiesNotFound);
        }

    }

    @Override
    public String getContainerId() {
        return containerId;
    }

    @Override
    public String getDemarcheId() {
        return demarcheId;
    }

    @Override
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
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
    public String getPaysRestUrl() {
        return paysRestUrl;
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
    public String getFileJwt() {
        return fileJwt;
    }

    @Override
    public String getMailJwt() {
        return mailJwt;
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
        return Long
                .parseLong(usagersCacheDuration);
    }

    @GouvIndexationProperty
    @Override
    public String getSearchHighlightPreTags() {
        String searchPreTags = esProperties.getPretags();
        return searchPreTags != null ? searchPreTags : "<b>";
    }

    @GouvIndexationProperty
    @Override
    public String getSearchHighlightPostTags() {
        String searchPostTags = esProperties.getPosttags();
        return searchPostTags != null ? searchPostTags : "</b>";
    }

    @GouvIndexationProperty
    @Override
    public String getEsUser() {
        return esProperties.getUser();
    }

    @GouvIndexationProperty
    @Override
    public String getEsPassword() {
        return esProperties.getPassword();
    }

    @GouvIndexationProperty
    @Override
    public String getEsClusterHosts() {
        return esProperties.getClusterHosts();
    }

    @GouvIndexationProperty
    @Override
    public Integer getEsPort() {
        String batchSize = esProperties.getPort();

        if (batchSize != null) {
            return Integer.parseInt(batchSize);
        }

        return null;
    }

    @GouvIndexationProperty
    @Override
    public Integer getEsReindexBulkSize() {
        String esReindexBulkSize = esProperties.getBulksize();

        if (esReindexBulkSize != null) {
            return Integer.parseInt(esReindexBulkSize);
        }

        return null;
    }

    @Override
    public Integer getEsConnectTimeout() {
        // Valeur par défaut de 30 secondes
        return Integer.parseInt(esProperties.getConnectTimeout());
    }

    @Override
    public Integer getEsSocketTimeout() {
        // Valeur par défaut de 30 secondes
        return Integer.parseInt(esProperties.getSocketTimeout());
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
        return Boolean.parseBoolean(kafkaProperties.getKafkaSSLEnabled());
    }

    @GouvSSLProperty
    @Override
    public String getGUKafkaSSLTrustStoreLocation() {
        return kafkaProperties.getTruststoreLocation();
    }

    @GouvSSLProperty
    @Override
    public String getGUKafkaSSLTrustStorePassword() {
        return kafkaProperties.getTruststorePassword();
    }

    @GouvSSLProperty
    @Override
    public String getGUKafkaSSLKeyStoreLocation() {
        return kafkaProperties.getKeystoreLocation();
    }

    @GouvSSLProperty
    @Override
    public String getGUKafkaSSLKeyStorePassword() {
        return kafkaProperties.getKeystorePassword();
    }

    @Override
    public boolean getKafkaEnabled() {
        return Boolean.parseBoolean(kafkaProperties.getKafkaEnabled());
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

    public String getApplicationPrefix() {
        return applicationPrefix;
    }

    @Override
    public String getProxyUrl() {
        return proxyProperties.getProxyUrl();
    }

    @Override
    public String getProxyPort() {
        return proxyProperties.getProxyPort();
    }

    @GouvArchivageProperty
    @Override
    public String getApiRioUrl() {
        return archivageProperties.getRioUrl();
    }

    @GouvArchivageProperty
    @Override
    public String getApiRioJwt() {
        return archivageProperties.getRioJwt();
    }

    @GouvArchivageProperty
    @Override
    public String getApiRioCodeAppli() {
        return archivageProperties.getRioCodeAppli();
    }

    @GouvArchivageProperty
    @Override
    public String getApiRioCodeNotice() {
        return archivageProperties.getRioCodeNotice();
    }

    @Override
    public boolean isPaiementEnabled() {
        return StringUtils.equals(paiementEnabled, "true");
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
	public String get2TiersBoUrl() {
		return _2tiersBoUrl;
	}

	@Override
	public String get2TiersBoJwt() {
		return _2tiersBoJwt;
	}
}
