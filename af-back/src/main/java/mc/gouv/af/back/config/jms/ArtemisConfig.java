package mc.gouv.af.back.config.jms;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.SecuritySettingPlugin;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.back.properties.GouvPropertiesResolver;

/*****
 * Classe pour surcharger la configuration de EmbeddedJMS{****
 * 
 * @link EmbeddedJMS} géré par bootstrap
 *
 *       Penser à mettre dans le fichier de properties les propriétés spring.artemis.mode=embedded et
 *       spring.artemis.embedded.remoteAccess=true
 *
 *       Cette configuration permet de sécuriser la connection avec un user / password
 *
 *       Seule la connexion native artemis peut être configurée (pour le moment)
 *       https://github.com/spring-projects/spring-boot/blob/v1.4.1.RELEASE/spring-boot-autoconfigure/src/main/java/org/
 *       springframework/boot/autoconfigure/jms/artemis/ArtemisEmbeddedServerConfiguration.java
 *
 * @author fgaujous
 *
 */
@Configuration
@Conditional(IndexationEnabledCondition.class)
@ConditionalOnProperty(prefix = "spring.artemis.embedded", name = "remoteAccess", havingValue = "true", matchIfMissing = false)
public class ArtemisConfig implements ArtemisConfigurationCustomizer {

    private static final String DEFAULT_TRANSPORT_PROTOCOLS = "artemis";

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtemisConfig.class);

    private final ArtemisProperties properties;

    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;
    /**
     * Voir pour gérer le multi protocoles
     */
    private String protocols;

    public ArtemisConfig(ArtemisProperties properties) {
        super();
        this.properties = properties;
    }

    // ...
    @Override
    public void customize(org.apache.activemq.artemis.core.config.Configuration configuration) {

        LOGGER.info("Paramétrage de ArtemisMQ pour le gouvernement");

        int port = gouvPropertiesResolver.getJmsPort();
        LOGGER.info("Port : " + port);

        protocols = StringUtils.isNotBlank(protocols) ? protocols : DEFAULT_TRANSPORT_PROTOCOLS;
        LOGGER.info("Protocols : " + protocols);

        Map<String, Object> connectionParams = new HashMap<>();
        // connectionParams.put(org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants.PORT_PROP_NAME,
        // port);
        connectionParams.put(TransportConstants.PORT_PROP_NAME, port);
        connectionParams.put(TransportConstants.PROTOCOLS_PROP_NAME, protocols);
        // Pour accepter toutes les demandes remote et pas seulement les localhost
        connectionParams.put(TransportConstants.HOST_PROP_NAME, "0.0.0.0");
        // connectionParams.put(TransportConstants.CONNECTION_TTL, )
        Set<TransportConfiguration> acceptors = configuration.getAcceptorConfigurations();
        TransportConfiguration tc = new TransportConfiguration(NettyAcceptorFactory.class.getName(), connectionParams);

        acceptors.add(tc);
        configuration.addConnectorConfiguration("nettyConnector",
                new TransportConfiguration(NettyConnectorFactory.class.getName()));
        configuration.setAcceptorConfigurations(acceptors);
        boolean persistent = this.properties.getEmbedded().isPersistent();
        LOGGER.info("Persistent : " + persistent);

        if (persistent) {
            configuration.setPersistenceEnabled(true);
            String dir = gouvPropertiesResolver.getJmsDataDir();

            configuration.setCreateBindingsDir(true);
            configuration.setCreateJournalDir(true);
            LOGGER.info("Dir : " + dir);
            configuration.setPagingDirectory(dir + File.separator + "paging");
            configuration.setBindingsDirectory(dir + File.separator + "bindings");
            configuration.setLargeMessagesDirectory(dir + File.separator + "large-messages");
            configuration.setJournalDirectory(dir + File.separator + "journal");

        }

        AddressSettings addressSettings = new AddressSettings();

        String redeliverayDelayStr = gouvPropertiesResolver.getJmsRedeliveryDelay();
        if (StringUtils.isNotBlank(redeliverayDelayStr)) {
            long redeliverayDelay = Long.parseLong(redeliverayDelayStr);

            LOGGER.info("RedeliverayDelay : " + redeliverayDelay);

            addressSettings.setRedeliveryDelay(redeliverayDelay);
        }

        String redeliverayMultiplierStr = gouvPropertiesResolver.getJmsRedeliveryMultiplier();
        if (StringUtils.isNotBlank(redeliverayMultiplierStr)) {
            double redeliverayMultiplier = Double.parseDouble(redeliverayMultiplierStr);
            addressSettings.setRedeliveryMultiplier(redeliverayMultiplier);
            LOGGER.info("RedeliverayMultiplier : " + redeliverayMultiplier);
        }

        String maxDeliveryAttempsStr = gouvPropertiesResolver.getJmsRedeliveryMaxAttemps();
        if (StringUtils.isNotBlank(maxDeliveryAttempsStr)) {
            int maxDeliveryAttemps = Integer.parseInt(maxDeliveryAttempsStr);
            LOGGER.info("MaxDeliveryAttemps : " + maxDeliveryAttemps);
            addressSettings.setMaxDeliveryAttempts(maxDeliveryAttemps);
        }

        String demJmsDlq = gouvPropertiesResolver.getJmsDlq();
        if (StringUtils.isNotBlank(demJmsDlq)) {

            String dlq = "jms.queue." + demJmsDlq;
            LOGGER.info("Paramétrage de la Dead Letter Queue : " + dlq);

            addressSettings.setDeadLetterAddress(new SimpleString(dlq));
        }

        // Pour toutes les queues
        LOGGER.info("Paramétrage utilisé pour toutes les queues : wildcard # sur addressesSettings");
        configuration.addAddressesSetting("#", addressSettings);

        // Securité

        List<SecuritySettingPlugin> ssplugins = new ArrayList<>();
        CustomArtemisSecuritySettingPlugin ssplugin = new CustomArtemisSecuritySettingPlugin(
                gouvPropertiesResolver.getJmsTopic());
        ssplugins.add(ssplugin);
        configuration.setSecuritySettingPlugins(ssplugins);

        configuration.setSecurityEnabled(true);

    }

}
