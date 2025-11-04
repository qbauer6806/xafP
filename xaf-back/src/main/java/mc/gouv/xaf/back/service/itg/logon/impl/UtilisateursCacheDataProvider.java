package mc.gouv.xaf.back.service.itg.logon.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.CacheService;
import mc.gouv.xaf.back.service.itg.logon.LogonClient;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.caching.GouvCacheDataProvider;
import mc.gouv.xaf.shared.dto.CacheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UtilisateursCacheDataProvider implements GouvCacheDataProvider<String, User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilisateursCacheDataProvider.class);

    private final LogonClient logonClient;
    private final CacheService cacheService;
    private final GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public ConcurrentHashMap<String, User> getAll() {
        LOGGER.info("Récupération des users...");
        ConcurrentHashMap<String, User> map = new ConcurrentHashMap<>();
        List<User> users = getUsers();
        if (users != null) {
            for (User user : users) {
                map.put(user.getMatricule(), user);
            }
        }
        return map;
    }

    @Override
    public User get(String key) {
        return logonClient.getUserByMatricule(key);
    }

    private List<User> getUsers() {
        CacheDTO logonUsersCache = cacheService.getCache("LOGON_USERS");
        long cacheDuration = gouvPropertiesResolver.getUtilisateursCacheDuration();
        ObjectMapper mapper = new ObjectMapper();

        List<User> users = null;
        // Si la valeur n'est pas présente en base ou est expirée, appeler l'API
        if (logonUsersCache == null || (new Date().after(new Date(logonUsersCache.getDateMaj().getTime() + cacheDuration)))) {
            LOGGER.info("Appel de l'API LOGON car JSON non présent en base ou expiré...");
            users = logonClient.getListUserByCodeAppli(gouvPropertiesResolver.getDemarcheId());
            if (logonUsersCache == null) {
                logonUsersCache = new CacheDTO();
                logonUsersCache.setPkCache("LOGON_USERS");
            }
            logonUsersCache.setData(mapper.valueToTree(users));
            cacheService.updateCache(logonUsersCache);
        }
        else {
            try {
                users = mapper.convertValue(
                        logonUsersCache.getData(), new TypeReference<>() {

                        }
                );
            } catch (IllegalArgumentException e) {
                LOGGER.error("Erreur lors de mapper.treeToValue() dans getUsers()", e);
            }
        }
        return users;
    }

}
