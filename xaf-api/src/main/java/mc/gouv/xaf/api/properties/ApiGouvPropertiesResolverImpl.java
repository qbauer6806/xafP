package mc.gouv.xaf.api.properties;

import mc.gouv.xaf.back.properties.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement.
 *
 * @author qdeme
 */
@Component
@Primary
@Profile("gouv")
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
