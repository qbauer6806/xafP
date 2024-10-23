package mc.gouv.xaf.backweb.properties;

import mc.gouv.xaf.back.properties.GouvPropertiesResolverImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composant permettant de récupérer des éléments de configuration propres au module backserver
 *
 * @author mpavone
 */
@Component
@Primary
@Profile("gouv")
@Transactional(rollbackFor = Exception.class)
public class BackGouvPropertiesResolverImpl extends GouvPropertiesResolverImpl implements BackGouvPropertiesResolver {

    ///// BACK PROPERTIES
    @Value("${mc.gouv.${application.name}.backserver.front.key:OPTIONAL}")
    private String frontSharedKey;

    @Value("${mc.gouv.${application.name}.backserver.front.formstartpage:OPTIONAL}")
    private String frontFormStartPage;

    @Value("${mc.gouv.backserver.help.url}")
    private String helpUrl;

    @Value("${mc.gouv.backserver.contact.support.url}")
    private String contactSupportUrl;

    @Value("${mc.gouv.backserver.env.name}")
    private String sharedEnv;

    @Value("${mc.gouv.backserver.env.color}")
    private String sharedEnvColor;

    @Value("${mc.gouv.backserver.env.displaystacktrace:false}")
    private String sharedEnvdisplayStackTrace;

    @Value("${mc.gouv.${application.name}.backserver.novalidate:false}")
    private String noValidate;

    @Value("${mc.gouv.${application.name}.backserver.gichkey.client_id}")
    private String gichkeyClientId;

    @Value("${mc.gouv.${application.name}.backserver.gichkey.client_secret}")
    private String gichkeyClientSecret;

    @Value("${mc.gouv.${application.name}.backserver.matomo.site_id}")
    private String matomoSiteId;

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
    public boolean getGouvSharedEnvDisplayStackTrace() {
        return Boolean.parseBoolean(sharedEnvdisplayStackTrace);
    }

    @Override
    public String getContactSupportUrl() {
        return contactSupportUrl;
    }

    @Override
    public boolean getNovalidate() {
        return Boolean.parseBoolean(noValidate);
    }

    @Override
    public String getGichkeyClientId() {
        return gichkeyClientId;
    }

    @Override
    public String getGichkeyClientSecret() {
        return gichkeyClientSecret;
    }

    public String getMatomoSiteId() {
        return matomoSiteId;
    }
}
