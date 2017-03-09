package mc.gouv.af.back.util;

import java.util.List;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.service.properties.GouvPropertiesResolver;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.logon.apiclient.UserRest;
import mc.gouv.logon.model.User;

@Profile("gouv")
@Component
public class UtilisateursCacheImpl implements UtilisateursCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilisateursCacheImpl.class);

    @Autowired
    DemClient demClient;

    @Autowired
    GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    CacheManager cacheManager;

    @Override
    @CacheResult(cacheName = "utilisateur")
    public User getUtilisateur(@CacheKey String matricule) {
        User agent = null;
        try {
            agent = UserRest.getUserByMatricule(matricule);

            return agent;
        } catch (Exception e) {
            LOGGER.error("Impossible de récupérer l'utilisateur matricule : " + matricule, e);
        }

        return null;
    }

    @Override
    @CacheResult(cacheName = "utilisateurAll")
    public List<User> getAll() {
        LOGGER.info("MISE A JOUR DU CACHE DES UTILISATEURS");
        List<User> agents = null;
        try {
            agents = UserRest.getListUserByCodeAppli(gouvPropertiesResolver.getDemarcheId());
            for (User u : agents) {
                cacheManager.getCache("utilisateur").put(u.getMatricule(), u);
            }
        } catch (Exception e) {
            LOGGER.error("Erreur à la récupération des utilisateurs", e);
        }

        return agents;

    }

    @Override
    @CacheRemoveAll(cacheName = "utilisateur")
    public void clearCache() {

    }
}
