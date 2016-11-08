package mc.gouv.af.back.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.Static;
import mc.gouv.af.back.service.properties.AfGouvProperty;
import mc.gouv.af.back.service.properties.GouvProperty;
import mc.gouv.af.back.service.properties.GouvPropertyNotFoundException;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement. Proxy vers Static.getValue()
 * permettant via Spring de mocker les appels à Static.getValue().
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class GouvPropertiesResolverImpl implements GouvPropertiesResolver {

    @PostConstruct
    private void verifyProperties() throws GouvPropertyNotFoundException {
        GouvProperty[] properties = AfGouvProperty.values();
        List<GouvProperty> propertiesMissing = new ArrayList<GouvProperty>();
        for (GouvProperty prop : properties) {
            if (StringUtils.isBlank(getValue(prop))) {
                propertiesMissing.add(prop);
            }
        }

        if (!propertiesMissing.isEmpty()) {
            throw new GouvPropertyNotFoundException(propertiesMissing);
        }
    }

    @Override
    public String getValue(GouvProperty key) {
        return Static.getValue(key.getCode());
    }

}
