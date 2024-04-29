package mc.gouv.xaf.backweb.properties;

import mc.gouv.xaf.back.properties.GouvPropertiesResolverImpl;
import org.apache.commons.lang3.StringUtils;
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

    @Value("${mc.gouv.${application.name}.backserver.help.url}")
    private String helpUrl;

    @Value("${mc.gouv.${application.name}.backserver.contactSupport.url}")
    private String contactSupportUrl;

    @Value("${mc.gouv.${application.name}.backserver.env}")
    private String sharedEnv;

    @Value("${mc.gouv.${application.name}.backserver.env.color}")
    private String sharedEnvColor;

    @Value("${mc.gouv.${application.name}.backserver.novalidate:false}")
    private String noValidate;

    @Value("${mc.gouv.${application.name}.backserver.gichkey.client_id}")
    private String gichkeyClientId;

    @Value("${mc.gouv.${application.name}.backserver.gichkey.client_secret}")
    private String gichkeyClientSecret;

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

}
