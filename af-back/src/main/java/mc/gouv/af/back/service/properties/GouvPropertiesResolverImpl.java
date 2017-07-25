package mc.gouv.af.back.service.properties;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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
                    String value = (String) method.invoke(this);
                    if (StringUtils.isBlank(value)) {
                        propertiesNotFound.add(propertyDescriptor.getReadMethod().toString());
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

    private static final String DEM_URL = "mc.gouv.af.back.dem.url";

    @Override
    public String getDemUrl() {
        return Static.getValue(DEM_URL);
    }
    
    @Override
    public String getDemJwt() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.dem.jwt");
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
    public String getFrontSharedKey() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.front.key");
    }

    @Override
    public String getHelpUrl() {
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.help.url");
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

    private static final String CONTACT_SUPPORT_URL = "mc.gouv.af.back.contactSupport.url";

    @Override
    public String getContactSupportUrl() {
        return Static.getValue(CONTACT_SUPPORT_URL);
    }
    
}
