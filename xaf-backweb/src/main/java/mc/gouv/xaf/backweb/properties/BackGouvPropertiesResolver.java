package mc.gouv.xaf.backweb.properties;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

public interface BackGouvPropertiesResolver extends GouvPropertiesResolver {

    String getGouvSharedEnv();

    String getGouvSharedEnvColor();

    boolean getGouvSharedEnvDisplayStackTrace();

    String getContactSupportUrl();

    String getFrontSharedKey();

    String getHelpUrl();

    String getFrontFormStartPage();

    boolean getNovalidate();

    String getGichkeyClientId();

    String getGichkeyClientSecret();

}
