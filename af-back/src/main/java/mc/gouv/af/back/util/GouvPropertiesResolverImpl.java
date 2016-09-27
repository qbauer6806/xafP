package mc.gouv.af.back.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.Static;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement.
 * Proxy vers Static.getValue() permettant via Spring de mocker les appels à Static.getValue().
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class GouvPropertiesResolverImpl implements GouvPropertiesResolver {

    @Override
    public String getValue(String key) {
        return Static.getValue(key);
    }

}
