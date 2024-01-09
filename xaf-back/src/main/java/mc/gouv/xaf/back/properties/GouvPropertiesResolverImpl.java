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

import mc.gouv.Static;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement. Proxy vers Static.getValue()
 * permettant via Spring de mocker les appels à Static.getValue().
 *
 * @author qdeme
 */
@Component
@Profile("gouv")
@Transactional(rollbackFor = Exception.class)
public class GouvPropertiesResolverImpl implements GouvPropertiesResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvPropertiesResolverImpl.class);
    private static final String MC_GOUV_PREFIX = "mc.gouv";
    private static final String MAX_BYTE = "20971520";

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
            String archivagePropStr = environment
                    .getProperty(MC_GOUV_PREFIX + applicationPrefix + ".archivage.enabled");
            boolean archivageEnabled = StringUtils.equals(archivagePropStr, "true");

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
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backserver.file.containerId");
    }

    @Override
    public String getDemarcheId() {
        return demarcheId;
    }

    @Override
    public String getProcessDefinitionKey() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backserver.processDefinitionKey");
    }

    private static final String FILE_URL = "mc.gouv.af.back.file.url";

    @Override
    public String getFileUrl() {
        return Static.getValue(FILE_URL);
    }

    public static final String VSCAN_URL = "mc.gouv.appfactory.external.vscan.url";

    @Override
    public String getVScanUrl() {
        return Static.getValue(VSCAN_URL);
    }

    private static final String PAYS_REST_URL = "mc.gouv.demarches.external.pays.url";

    @Override
    public String getPaysRestUrl() {
        return Static.getValue(PAYS_REST_URL);
    }

    private static final String MAIL_URL = "mc.gouv.af.back.mail.url";

    @Override
    public String getVscanJwt() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backserver.vscan.jwt");
    }

    @Override
    public String getMailUrl() {
        return Static.getValue(MAIL_URL);
    }

    @Override
    public String getFileJwt() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backserver.file.jwt");
    }

    @Override
    public String getMailJwt() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backserver.mail.jwt");
    }

    @Override
    public String getFrontUrl() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backserver.front.url");
    }

    @Override
    public String getBackUrl() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backserver.back.url");
    }

    @Override
    public String getFrontSharedKey() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backserver.front.key");
    }

    @Override
    public String getHelpUrl() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backserver.help.url");
    }

    @Override
    public String getFrontFormStartPage() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backserver.front.formstartpage");
    }

    private static final String GOUV_SHARED_ENV = "mc.gouv.shared.env";

    @Override
    public String getGouvSharedEnv() {
        return Static.getValue(GOUV_SHARED_ENV);
    }

    private static final String GOUV_SHARED_ENV_COLOR = "mc.gouv.shared.env.color";

    @Override
    public String getGouvSharedEnvColor() {
        return Static.getValue(GOUV_SHARED_ENV_COLOR);
    }

    private static final String GOUV_SHARED_LOGON_URL = "mc.gouv.shared.backserver.logon.url";

    @Override
    public String getGouvSharedLogonUrl() {
        return Static.getValue(GOUV_SHARED_LOGON_URL);
    }

    private static final String CONTACT_SUPPORT_URL = "mc.gouv.af.back.contactSupport.url";

    @Override
    public String getContactSupportUrl() {
        return Static.getValue(CONTACT_SUPPORT_URL);
    }

    @Override
    public long getUsagersCacheDuration() {
        return Long
                .parseLong(Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backserver.usagerscache.duration"));
    }

    @GouvIndexationProperty
    @Override
    public String getSearchHighlightPreTags() {
        String searchPreTags = Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".search.highlight.pretags");
        return searchPreTags != null ? searchPreTags : "<b>";
    }

    @GouvIndexationProperty
    @Override
    public String getSearchHighlightPostTags() {
        String searchPostTags = Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".search.highlight.posttags");
        return searchPostTags != null ? searchPostTags : "</b>";
    }

    @GouvIndexationProperty
    @Override
    public String getEsUser() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".elasticsearch.user");
    }

    @GouvIndexationProperty
    @Override
    public String getEsPassword() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".elasticsearch.password");
    }

    @GouvIndexationProperty
    @Override
    public String getEsClusterHosts() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".elasticsearch.clusterHosts");
    }

    @GouvIndexationProperty
    @Override
    public Integer getEsPort() {
        String batchSize = Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".elasticsearch.port");

        if (batchSize != null) {
            return Integer.parseInt(batchSize);
        }

        return null;
    }

    @GouvIndexationProperty
    @Override
    public Integer getEsReindexBulkSize() {
        String esReindexBulkSize = Static
                .getValue(MC_GOUV_PREFIX + applicationPrefix + ".elasticsearch.reindex.bulksize");

        if (esReindexBulkSize != null) {
            return Integer.parseInt(esReindexBulkSize);
        }

        return null;
    }

    @Override
    public Integer getEsConnectTimeout() {
        String connectTimeout = Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".elasticsearch.connectTimeout");

        if (StringUtils.isNotBlank(connectTimeout)) {
            return Integer.parseInt(connectTimeout);
        }

        // Valeur par défaut de 30 secondes
        return 30000;
    }

    @Override
    public Integer getEsSocketTimeout() {
        String socketTimeout = Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".elasticsearch.socketTimeout");

        if (StringUtils.isNotBlank(socketTimeout)) {
            return Integer.parseInt(socketTimeout);
        }

        // Valeur par défaut de 30 secondes
        return 30000;
    }

    @Override
    public boolean getNovalidate() {
        String value = Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".novalidate");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public Integer getUsagersPageSize() {
        String pageSize = Static.getValue("mc.gouv.demarches.external.usagers.pagesize");

        if (StringUtils.isNotBlank(pageSize)) {
            return Integer.parseInt(pageSize);
        }

        // Valeur par défaut de 500 usagers par page
        return 500;
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
        return Static.getValue("mc.gouv.af.back.external.gichuni.kafka.bootstrapserversconfig");
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public boolean getGUKafkaSSLEnabled() {
        String value = Static.getValue("mc.gouv.af.back.external.gichuni.kafka.ssl.enabled");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    @GouvSSLProperty
    @Override
    public String getGUKafkaSSLTrustStoreLocation() {
        return Static.getValue("mc.gouv.af.back.external.gichuni.kafka.ssl.truststore.location");
    }

    @GouvSSLProperty
    @Override
    public String getGUKafkaSSLTrustStorePassword() {
        return Static.getValue("mc.gouv.af.back.external.gichuni.kafka.ssl.truststore.password");
    }

    @GouvSSLProperty
    @Override
    public String getGUKafkaSSLKeyStoreLocation() {
        return Static.getValue("mc.gouv.af.back.external.gichuni.kafka.ssl.keystore.location");
    }

    @GouvSSLProperty
    @Override
    public String getGUKafkaSSLKeyStorePassword() {
        return Static.getValue("mc.gouv.af.back.external.gichuni.kafka.ssl.keystore.password");
    }

    @Override
    public boolean getKafkaEnabled() {
        String value = Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backapi.kafka.enabled");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public String getGichkeyUrl() {
        return Static.getValue("mc.gouv.af.back.external.gichkey.url");
    }

    @Override
    public String getGichuniUrl() {
        return Static.getValue("mc.gouv.af.back.external.gichuni.url");
    }

    @Override
    public String getGichkeyClientId() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".external.gichkey.client_id");
    }

    @Override
    public String getGichkeyClientSecret() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".external.gichkey.client_secret");
    }

    @Override
    public String getGUKafkaProducerMaxRequestSize() {
        String value = Static
                .getValue(MC_GOUV_PREFIX + applicationPrefix + ".backapi.kafka.producer.maxrequestsizeconfig");
        if (value == null) {
            return MAX_BYTE;
        }
        return value;
    }

    @Override
    public String getGUKafkaConsumerFetchMaxBytes() {
        String value = Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".backapi.kafka.consumer.fetchmaxbytes");
        if (value == null) {
            return MAX_BYTE;
        }
        return value;
    }

    @Override
    public String getGUKafkaConsumerMaxPartitionFetchBytes() {
        String value = Static
                .getValue(MC_GOUV_PREFIX + applicationPrefix + ".backapi.kafka.consumer.maxpartitionfetchbytes");
        if (value == null) {
            return MAX_BYTE;
        }
        return value;
    }

    public String getApplicationPrefix() {
        return applicationPrefix;
    }

    @Override
    public String getProxyUrl() {
        return Static.getValue("ADR-IP-PROXY");
    }

    @Override
    public String getProxyPort() {
        return Static.getValue("PORT-PROXY");
    }

    @GouvArchivageProperty
    @Override
    public String getApiRioUrl() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".rio.url");
    }

    @GouvArchivageProperty
    @Override
    public String getApiRioJwt() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".rio.jwt");
    }

    @GouvArchivageProperty
    @Override
    public String getApiRioCodeAppli() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".rio.codeAppli");
    }

    @GouvArchivageProperty
    @Override
    public String getApiRioCodeNotice() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".rio.codeNotice");
    }

    @Override
    public boolean isPaiementEnabled() {
        String paiementProviderStr = environment.getProperty(MC_GOUV_PREFIX + applicationPrefix + ".paiement.enabled");
        return StringUtils.equals(paiementProviderStr, "true");
    }

    public  String getPorteDocUrl() {
        String value = getGichuniUrl();
        return StringUtils.isBlank(value) ? "vide" : value + "/public/doc-holder";
    }
        
    @Override
    public String getApiUlisMoyensGenerauxUrl() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".ulis.url.moyens-generaux", "N/D");
    }

    @Override
    public String getApiUlisTiersOrganisationUrl() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".ulis.url.tiers-organisation", "N/D");
    }

    @Override
    public String getApiUlisAuthenticationUser() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".ulis.authentication.user", "N/D");
    }

    @Override
    public String getApiUlisAuthenticationPassword() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".ulis.authentication.password", "N/D");
    }

    @Override
    public String getApiUlisFunctionalUser() {
        return Static.getValue(MC_GOUV_PREFIX + applicationPrefix + ".ulis.account", "N/D");
    }
}
