package mc.gouv.xaf.back.service.itg.logon;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class UtilisateursCache {

    @Autowired
    private LogonClient logonClient;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Cacheable("utilisateursCache")
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

    @Cacheable(value = "utilisateursCache", key = "#key")
    public User get(String key) {
        return logonClient.getUserByMatricule(key);
    }

    @CachePut(value = "usersCache", key = "#matricule")
    public void add(String matricule, User user) {
        // pas besoin de faire du traitement
    }

}
