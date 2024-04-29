package mc.gouv.xaf.api.properties;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

public interface ApiGouvPropertiesResolver extends GouvPropertiesResolver {

    String getGichkeyClientId();

    String getGichkeyClientSecret();
}
