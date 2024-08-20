package mc.gouv.xaf.xaf12batch.logon;

import feign.FeignException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import mc.gouv.xaf.xaf12batch.logon.dto.User;
import mc.gouv.xboot.caching.GouvCacheDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UtilisateursCacheLogonDataProvider implements GouvCacheDataProvider<String, User>, UtilisateursCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilisateursCacheLogonDataProvider.class);

    @Autowired
    private LogonClient logonClient;

    @Value("${application.name}")
    private String applicationName;

    public ConcurrentMap<String, User> getAll() {
        ConcurrentHashMap<String, User> map = new ConcurrentHashMap();

        List<User> users = logonClient.getListUserByCodeAppli(applicationName);
        if (users != null) {

            for (User user : users) {
                map.put(user.getMatricule(), user);
            }
        }

        return map;
    }


    public User get(String key) {
        try {
            return logonClient.getUserByMatricule(key);
        } catch (FeignException.NotFound e) {
            // Log l'erreur et retourner une valeur par défaut ou null
            LOGGER.warn("Utilisateur avec le matricule {} non trouvé.", key);
            return null; // ou vous pouvez lancer une exception personnalisée
        }
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
