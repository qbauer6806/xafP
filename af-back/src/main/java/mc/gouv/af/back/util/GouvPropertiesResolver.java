package mc.gouv.af.back.util;

import mc.gouv.af.back.service.properties.GouvProperty;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement. Proxy vers Static.getValue()
 * permettant via Spring de mocker les appels à Static.getValue().
 * 
 * @author qdeme
 *
 */
public interface GouvPropertiesResolver {

    public String getValue(GouvProperty key);

}
