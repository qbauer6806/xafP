package mc.gouv.xaf.api.properties;

import mc.gouv.xaf.back.properties.GouvPropertiesResolverImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement.
 *
 * @author qdeme
 */
@Component
@Primary
@Transactional(rollbackFor = Exception.class)
public class ApiGouvPropertiesResolverImpl extends GouvPropertiesResolverImpl implements ApiGouvPropertiesResolver {

    @Value("${mc.gouv.${application.name}.apiserver.gichkey.client_id}")
    private String gichkeyClientId;

    @Value("${mc.gouv.${application.name}.apiserver.gichkey.client_secret}")
    private String gichkeyClientSecret;

    @Override
    public String getGichkeyClientId() {
        return gichkeyClientId;
    }

    @Override
    public String getGichkeyClientSecret() {
        return gichkeyClientSecret;
    }
}
