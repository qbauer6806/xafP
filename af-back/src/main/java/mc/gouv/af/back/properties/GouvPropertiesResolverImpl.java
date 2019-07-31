package mc.gouv.af.back.properties;

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

                    // On ignore la présence de la property si la méthode possède @GouvIndexationProperty mais que l'appli a indexationEnabled=false
                    if (!(method.getDeclaredAnnotation(GouvIndexationProperty.class) instanceof GouvIndexationProperty)
                            || (method.getDeclaredAnnotation(
                                    GouvIndexationProperty.class) instanceof GouvIndexationProperty
                                    && indexingEnabled)) {
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
    public Integer getJmsPort() {

        String port = Static.getValue("mc.gouv" + applicationPrefix + ".jms.port");

        if (port != null) {
            return Integer.parseInt(port);
        }

        return null;

    }

    @GouvIndexationProperty
    @Override
    public String getJmsDataDir() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.data.dir");
    }

    @GouvIndexationProperty
    @Override
    public String getJmsRedeliveryDelay() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.redelivery.delay");
    }

    @GouvIndexationProperty
    @Override
    public String getJmsRedeliveryMultiplier() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.redelivery.multiplier");
    }

    @GouvIndexationProperty
    @Override
    public String getJmsRedeliveryMaxAttemps() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.redelivery.maxAttemps");
    }

    @GouvIndexationProperty
    @Override
    public String getJmsDlq() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.dlq");
    }

    @GouvIndexationProperty
    @Override
    public String getJmsTopic() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.topic");
    }

    @GouvIndexationProperty
    @Override
    public String getJmsHost() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.host");
    }

    @GouvIndexationProperty
    @Override
    public String getJmsSenderUser() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.topic.sender.user");
    }

    @GouvIndexationProperty
    @Override
    public String getJmsSenderPassword() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.topic.sender.password");
    }

    @GouvIndexationProperty
    @Override
    public String getJmsConsumerUser() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.topic.consumer.user");
    }

    @GouvIndexationProperty
    @Override
    public String getJmsConsumerPassword() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.topic.consumer.password");
    }

    @GouvIndexationProperty
    @Override
    public String getSubscriptionKey() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".jms.topic.subscription.key");
    }

    @GouvIndexationProperty
    @Override
    public String getEsClusterName() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".elasticsearch.clustername");
    }

    @GouvIndexationProperty
    @Override
    public String getEsHost() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".elasticsearch.host");
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
    public String getEsNodeName() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".elasticsearch.nodename");
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
    public boolean getNovalidate() {
        String value = Static.getValue("mc.gouv" + applicationPrefix + ".novalidate");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}
