package mc.gouv.xaf.back.properties;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private static final String MC_GOUV_PREFIX = "mc.gouv";

    /*
     * hab
     */
    @Value("${application.name}")
    private String applicationName;

    @Value("${application.module}")
    private String applicationModule;

    /**
     * Uppercase de application.name
     */
    private String demarcheId;

    @Autowired
    private Environment environment;

    /*
     * .hab Sert à gérer s'il n'y a pas de fichier config.properties alors nous prenons en compte les properties sans
     * prefix ex : mc.gouv.appfactory.url et non pas mc.gouv.appfactory.hab.url
     */
    private String applicationPrefix = StringUtils.EMPTY;


    ///// REFACTOR PROPERTIES

    @Value("${mc.gouv.${application.name}.backserver.file.containerId}")
    private String containerId;

    @Value("${mc.gouv.${application.name}.backserver.processDefinitionKey}")
    private String processDefinitionKey;

    @Value("${mc.gouv.appfactory.external.vscan.url}")
    private String vscanUrl;

    @Value("${mc.gouv.${application.name}.backserver.vscan.jwt}")
    private String vscanJwt;

    @Value("${mc.gouv.af.back.mail.url}")
    private String mailUrl;

    @Value("${mc.gouv.${application.name}.backserver.mail.jwt}")
    private String mailJwt;

    @Value("${mc.gouv.af.back.file.url}")
    private String fileUrl;

    @Value("${mc.gouv.${application.name}.backserver.file.jwt}")
    private String fileJwt;

    @Value("${mc.gouv.${application.name}.backserver.usagerscache.duration}")
    private String usagersCacheDuration;

    @Autowired
    private ElasticsearchProperties esProperties;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private ProxyProperties proxyProperties;

    @Autowired
    private ArchivageProperties archivageProperties;

    @Value("${mc.gouv.${application.name}.paiement.enabled:false}")
    private String paiementEnabled;

    @Value("${mc.gouv.af.back.contactSupport.url}")
    private String contactSupportUrl;

    @Value("${mc.gouv.demarches.external.pays.url}")
    private String paysRestUrl;

    @Value("${mc.gouv.demarches.external.usagers.pagesize:500}")
    private String usagersPageSize;

    // MonGuichet
    @Value("${mc.gouv.${application.name}.external.gichkey.client_id}")
    private String gichkeyClientId;

    @Value("${mc.gouv.${application.name}.external.gichkey.client_secret}")
    private String gichkeyClientSecret;

    @Value("${mc.gouv.af.back.external.gichkey.url}")
    private String gichkeyUrl;

    @Value("${mc.gouv.af.back.external.gichuni.url}")
    private String gichuniUrl;

    @Value("${mc.gouv.piwik.internal.piwikUrl}")
    private String piwikUrl;

    @Value("${mc.gouv.piwik.internal.inc.piwikSiteId}")
    private String piwikSiteId;

    // Back only
    @Value("${mc.gouv.${application.name}.backserver.front.url:OPTIONAL}")
    private String frontUrl;

    @Value("${mc.gouv.${application.name}.backserver.front.key:OPTIONAL}")
    private String frontSharedKey;

    @Value("${mc.gouv.${application.name}.backserver.front.formstartpage:OPTIONAL}")
    private String frontFormStartPage;

    @Value("${mc.gouv.${application.name}.backserver.back.url:OPTIONAL}")
    private String backUrl;

    @Value("${mc.gouv.${application.name}.backserver.help.url:OPTIONAL}")
    private String helpUrl;

    @Value("${mc.gouv.shared.env:OPTIONAL}")
    private String sharedEnv;

    @Value("${mc.gouv.shared.env.color:OPTIONAL}")
    private String sharedEnvColor;

    @Value("${mc.gouv.shared.backserver.logon.url:OPTIONAL}")
    private String logonUrl;

    @Value("${mc.gouv.logon.rest.client.logonRestServerURI:OPTIONAL}")
    private String logonRestServerUrl;

    // Ulis
    @Value("${mc.gouv.${application.name}.ulis.url.moyens-generaux:OPTIONAL}")
    private String apiUlisMoyensGenerauxUrl;

    @Value("${mc.gouv.${application.name}.ulis.url.tiers-organisation:OPTIONAL}")
    private String apiUlisTiersOrganisationUrl;

    @Value("${mc.gouv.${application.name}.ulis.authentication.user:OPTIONAL}")
    private String apiUlisAuthentUser;

    @Value("${mc.gouv.${application.name}.ulis.authentication.password:OPTIONAL}")
    private String apiUlisAuthentPassword;

    @Value("${mc.gouv.${application.name}.ulis.account:OPTIONAL}")
    private String ulisFunctionalAccount;

    // Specifique
    @Value("${mc.gouv.${application.name}.novalidate:false}")
    private String noValidate;
    
    @Value("${mc.gouv.${application.name}.2tiers.bo.url:OPTIONAL}")
    private String _2tiersBoUrl;
    
    @Value("${mc.gouv.${application.name}.2tiers.bo.jwt:OPTIONAL}")
    private String _2tiersBoJwt;

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
            String indexingPropStr = environment.getProperty(MC_GOUV_PREFIX + applicationPrefix + ".indexing.enabled");
            boolean indexingEnabled = StringUtils.equals(indexingPropStr, "true");

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
    public String getFrontSharedKey() {
        return frontSharedKey;
    }

    @Override
    public String getHelpUrl() {
        return helpUrl;
    }

    @Override
    public String getFrontFormStartPage() {
        return frontFormStartPage;
    }

    @Override
    public String getGouvSharedEnv() {
        return sharedEnv;
    }

    @Override
    public String getGouvSharedEnvColor() {
        return sharedEnvColor;
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
    public String getPiwikUrl() {
        return piwikUrl;
    }

    @Override
    public String getPiwikSiteId() {
        return piwikSiteId;
    }
    @Override
    public String getContactSupportUrl() {
        return contactSupportUrl;
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
    public boolean getNovalidate() {
        return Boolean.parseBoolean(noValidate);
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
    public String getGichkeyClientId() {
        return gichkeyClientId;
    }

    @Override
    public String getGichkeyClientSecret() {
        return gichkeyClientSecret;
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

    public  String getPorteDocUrl() {
        String value = getGichuniUrl();
        return StringUtils.isBlank(value) ? "vide" : value + "/public/doc-holder";
    }

    @Override
    public String getApiUlisMoyensGenerauxUrl() {
        return apiUlisMoyensGenerauxUrl;
    }

    @Override
    public String getApiUlisTiersOrganisationUrl() {
        return apiUlisTiersOrganisationUrl;
    }

    @Override
    public String getApiUlisAuthenticationUser() {
        return apiUlisAuthentUser;
    }

    @Override
    public String getApiUlisAuthenticationPassword() {
        return apiUlisAuthentPassword;
    }

    @Override
    public String getApiUlisFunctionalUser() {
        return ulisFunctionalAccount;
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
