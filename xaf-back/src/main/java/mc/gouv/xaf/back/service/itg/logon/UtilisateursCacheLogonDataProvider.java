package mc.gouv.xaf.back.service.itg.logon;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.caching.GouvCacheDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UtilisateursCacheLogonDataProvider implements GouvCacheDataProvider<String, User>, UtilisateursCache {

    @Autowired
    private LogonClient logonClient;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    public ConcurrentMap<String, User> getAll() {
        ConcurrentHashMap<String, User> map = new ConcurrentHashMap();

        List<User> users = logonClient.getListUserByCodeAppli(gouvPropertiesResolver.getDemarcheId());
        if (users != null) {

            for (User user : users) {
                map.put(user.getMatricule(), user);
            }
        }

        return map;
    }


    public User get(String key) {
        return logonClient.getUserByMatricule(key);
    }

    @Override
    public User get(String key, boolean forceUpdate) {
        return null;
    }

    @Override
    public void refresh() {
        // not needed
    }

    @Override
    public void add(String key, User value) {
        // not needed
    }

    @Override
    public Collection<User> getValues() {
        return List.of();
    }

    @Override
    public Collection<String> getKeys() {
        return List.of();
    }
}
