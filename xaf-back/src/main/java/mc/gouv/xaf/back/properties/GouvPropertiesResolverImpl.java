package mc.gouv.xaf.back.properties;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
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
 *
 */
@Component
@Profile("gouv")
@Transactional(rollbackFor = Exception.class)
public class GouvPropertiesResolverImpl implements GouvPropertiesResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvPropertiesResolverImpl.class);

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
     * .hab
     * Sert à gérer s'il n'y a pas de fichier config.properties alors 
     * nous prenons en compte les properties sans prefix ex : mc.gouv.appfactory.url et non pas mc.gouv.appfactory.hab.url
     */
    private String applicationPrefix = StringUtils.EMPTY;

    @PostConstruct
    private void initPrefix() throws IntrospectionException, IllegalAccessException, InvocationTargetException,
            GouvPropertyNotFoundException {

        if (StringUtils.isNotBlank(applicationName)) {
            applicationPrefix = "." + applicationName;
            demarcheId = StringUtils.upperCase(applicationName);
        }

        //Vérification que chaque propriété a bien été configurée
        List<String> propertiesNotFound = new ArrayList<String>();
        try {

            for (PropertyDescriptor propertyDescriptor : Introspector
                    .getBeanInfo(GouvPropertiesResolverImpl.class, Object.class).getPropertyDescriptors()) {

                // propertyEditor.getReadMethod() exposes the getter
                // btw, this may be null if you have a write-only property

                java.lang.reflect.Method method = null;
                try {
                    LOGGER.info("Vérification de la propriété via le get : {}", propertyDescriptor.getReadMethod());
                    method = propertyDescriptor.getReadMethod();
                } catch (SecurityException e) {
                    LOGGER.error("Erreur lors de la récupération de la méthode", e);
                    throw e;
                }

                try {

                    // Est-ce que l'indexation est activée ?
                    String indexingPropStr = environment
                            .getProperty("mc.gouv" + applicationPrefix + ".indexing.enabled");
                    boolean indexingEnabled = false;
                    if (StringUtils.isNotBlank(indexingPropStr) && indexingPropStr.equals(true)) {
                        indexingEnabled = true;
                    }
                    
                    // Est-ce que le SSL est activé ?
                    boolean sslEnabled = getGUKafkaSSLEnabled();

                    // On ignore la présence de la property si la méthode possède @GouvIndexationProperty mais que l'appli a indexationEnabled=false
                    boolean pasIgnorerIndexing = !(method.getDeclaredAnnotation(GouvIndexationProperty.class) instanceof GouvIndexationProperty)
                            || (method.getDeclaredAnnotation(
                                    GouvIndexationProperty.class) instanceof GouvIndexationProperty
                                    && indexingEnabled);
                    
                    // On ignore la présence de la property si la méthode possède @GouvSSLProperty mais que l'appli a
                	// mc.gouv.af.back.external.gichuni.kafka.ssl.enabled=false
                    boolean pasIgnorerSSL = !(method.getDeclaredAnnotation(GouvSSLProperty.class) instanceof GouvSSLProperty)
                            || (method.getDeclaredAnnotation(
                            		GouvSSLProperty.class) instanceof GouvSSLProperty
                                    && sslEnabled);
                    if (pasIgnorerIndexing && pasIgnorerSSL) {
                        Object value = method.invoke(this);
                        if (value instanceof String) {
                            if (StringUtils.isBlank((String) value)) {
                                propertiesNotFound.add(propertyDescriptor.getReadMethod().toString());
                            }
                        } else if (value == null) {
                            propertiesNotFound.add(propertyDescriptor.getReadMethod().toString());
                        }
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Erreur lors de l'invocation de la méthode", e);
                    throw e;
                } catch (IllegalAccessException e) {
                    LOGGER.error("Erreur lors de l'invocation de la méthode", e);
                    throw e;
                } catch (InvocationTargetException e) {
                    LOGGER.error("Erreur lors de l'invocation de la méthode", e);
                    throw e;
                }

            }

        } catch (IntrospectionException e) {
            LOGGER.error("Erreur lors de l'introspection", e);
            throw e;
        }

        if (!propertiesNotFound.isEmpty()) {
            throw new GouvPropertyNotFoundException(propertiesNotFound);
        }
        //Map<String, String> properties = BeanUtils.describe(GouvPropertiesResolverImpl.class);

    }

    @Override
    public String getContainerId() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.file.containerId");
    }

    @Override
    public String getDemarcheId() {
        return demarcheId;
    }

    @Override
    public String getProcessDefinitionKey() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.processDefinitionKey");
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

    private static final String USAGERS_REST_URL = "mc.gouv.demarches.external.usagers.url";

    @Override
    public String getUsagersRestUrl() {
        return Static.getValue(USAGERS_REST_URL);
    }

    private static final String PAYS_REST_URL = "mc.gouv.demarches.external.pays.url";

    @Override
    public String getPaysRestUrl() {
        return Static.getValue(PAYS_REST_URL);
    }

    private static final String MAIL_URL = "mc.gouv.af.back.mail.url";

    @Override
    public String getVscanJwt() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.vscan.jwt");
    }

    @Override
    public String getMailUrl() {
        return Static.getValue(MAIL_URL);
    }

    @Override
    public String getFileJwt() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.file.jwt");
    }

    @Override
    public String getMailJwt() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.mail.jwt");
    }

    @Override
    public String getFrontUrl() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.front.url");
    }

    @Override
    public String getBackUrl() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.back.url");
    }

    @Override
    public String getFrontSharedKey() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.front.key");
    }

    @Override
    public String getHelpUrl() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.help.url");
    }

    @Override
    public String getFrontFormStartPage() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.front.formstartpage");
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
        return Long.parseLong(Static.getValue("mc.gouv" + applicationPrefix + ".backserver.usagerscache.duration"));
    }

    @GouvIndexationProperty
    @Override
    public String getSearchHighlightPreTags() {
        String searchPreTags = Static.getValue("mc.gouv" + applicationPrefix + ".search.highlight.pretags");
        return searchPreTags != null ? searchPreTags : "<b>";
    }

    @GouvIndexationProperty
    @Override
    public String getSearchHighlightPostTags() {
        String searchPostTags = Static.getValue("mc.gouv" + applicationPrefix + ".search.highlight.posttags");
        return searchPostTags != null ? searchPostTags : "</b>";
    }

    @GouvIndexationProperty
    @Override
    public String getEsUser() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".elasticsearch.user");
    }

    @GouvIndexationProperty
    @Override
    public String getEsPassword() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".elasticsearch.password");
    }

    @GouvIndexationProperty
    @Override
    public String getEsClusterHosts() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".elasticsearch.clusterHosts");
    }

    @GouvIndexationProperty
    @Override
    public Integer getEsPort() {
        String batchSize = Static.getValue("mc.gouv" + applicationPrefix + ".elasticsearch.port");

        if (batchSize != null) {
            return Integer.parseInt(batchSize);
        }

        return null;
    }

    @GouvIndexationProperty
    @Override
    public Integer getEsReindexBulkSize() {
        String esReindexBulkSize = Static.getValue("mc.gouv" + applicationPrefix + ".elasticsearch.reindex.bulksize");

        if (esReindexBulkSize != null) {
            return Integer.parseInt(esReindexBulkSize);
        }

        return null;
    }

    @Override
    public Integer getEsConnectTimeout() {
        String connectTimeout = Static.getValue("mc.gouv" + applicationPrefix + ".elasticsearch.connectTimeout");

        if (StringUtils.isNotBlank(connectTimeout)) {
            return Integer.parseInt(connectTimeout);
        }

        // Valeur par défaut de 30 secondes
        return 30000;
    }

    @Override
    public Integer getEsSocketTimeout() {
        String socketTimeout = Static.getValue("mc.gouv" + applicationPrefix + ".elasticsearch.socketTimeout");

        if (StringUtils.isNotBlank(socketTimeout)) {
            return Integer.parseInt(socketTimeout);
        }

        // Valeur par défaut de 30 secondes
        return 30000;
    }

    @Override
    public boolean getNovalidate() {
        String value = Static.getValue("mc.gouv" + applicationPrefix + ".novalidate");
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
        String value = Static.getValue("mc.gouv" + applicationPrefix + ".backapi.kafka.enabled");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

}
