package mc.gouv.af.back.util;

import java.util.ArrayList;
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
import mc.gouv.dem.apishared.model.UsagerCourrierDTO;
import mc.gouv.servicerest.usager.ReferentielUsagersClient;
import mc.gouv.servicerest.usager.model.UsagerBean;

@Profile("gouv")
@Component
public class UsagersCacheImpl implements UsagersCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersCacheImpl.class);

    public static final int USAGERID_OFFSET = 1000000000;

    @Autowired
    ReferentielUsagersClient referentielUsagersClient;

    @Autowired
    DemClient demClient;

    @Autowired
    GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    CacheManager cacheManager;

    @Override
    @CacheResult(cacheName = "usager")
    public UsagerBean getUsager(@CacheKey Integer usagerId) {

        LOGGER.info("Récupération de l'usager {}", usagerId);
        UsagerBean usager = null;
        try {
            if (!isUsagerCourrier(usagerId)) {
                LOGGER.info("Récupération d'un usager INTERNET");
                usager = referentielUsagersClient.getUsager(usagerId);

            } else {
                LOGGER.info("Récupération d'un usager COURRIER");
                UsagerCourrierDTO uc = demClient.getUsagerCourrier(gouvPropertiesResolver.getDemarcheId(), usagerId);
                usager = convertUsagerCourrierDTOToUsagerBean(uc);

            }

            return usager;
        } catch (Exception e) {
            LOGGER.error("Impossible de récupérer l'usager id : " + usagerId, e);
        }

        return null;
    }

    @Override
    @CacheResult(cacheName = "usagerAll")
    public List<UsagerBean> getAll() {

        LOGGER.info("MISE A JOUR DU CACHE DES USAGERS");
        List<UsagerBean> usagers = new ArrayList<UsagerBean>();
        try {
            //Récupération de tous les ids des usagers
            List<Integer> usagersIds = demClient.getUsagersIds(gouvPropertiesResolver.getDemarcheId());
            List<Integer> usagersCourriersIds = new ArrayList<Integer>();
            List<Integer> usagersInternetIds = new ArrayList<Integer>();

            for (Integer usagerId : usagersIds) {
                if (!isUsagerCourrier(usagerId)) {
                    usagersInternetIds.add(usagerId);

                } else {
                    usagersCourriersIds.add(usagerId);

                }
            }
            LOGGER.info("Récupération des usagers INTERNET: {}", usagersInternetIds);
            LOGGER.info("Récupération des usagers COURRIER: {}", usagersCourriersIds);
            //Si des usagers se sont désinscrits, il m'en sortira moins que le nombre d'ids donnés en paramètre
            if (!usagersInternetIds.isEmpty()) {
                if (usagersInternetIds.size() == 1) {
                    UsagerBean usagerBean = referentielUsagersClient.getUsager(usagersInternetIds.get(0));
                    if (usagerBean != null) {
                        usagers.add(usagerBean);
                    }

                } else {
                    usagers = referentielUsagersClient.getUsagers(usagersInternetIds);
                    //usagers peut être null
                    if (usagers == null) {
                        usagers = new ArrayList<UsagerBean>();
                    }
                }
            }

            List<UsagerBean> usagersCourriers = new ArrayList<UsagerBean>();
            //Voir pour faire la fonction qui prend une liste d'ids
            for (Integer usagerCourrierId : usagersCourriersIds) {
                LOGGER.debug("getUsagerFromID(" + usagerCourrierId + ") : Appel à DEM car usager courrier...");

                UsagerCourrierDTO uc = demClient.getUsagerCourrier(gouvPropertiesResolver.getDemarcheId(),
                        usagerCourrierId);
                UsagerBean ub = convertUsagerCourrierDTOToUsagerBean(uc);

                usagersCourriers.add(ub);

            }
            //Ajout des usagers courriers à la liste
            usagers.addAll(usagersCourriers);
            for (UsagerBean u : usagers) {
                cacheManager.getCache("usager").put(u.getId(), u);
            }

            LOGGER.info(usagers.toString());

        } catch (Exception e) {
            LOGGER.error("Erreur lors de la mise en cache des usagers", e);
        }
        return usagers;

    }

    @Override
    @CacheRemoveAll(cacheName = "usager")
    public void clearCache() {

    }

    public static boolean isUsagerCourrier(Integer usagerId) {
        return usagerId > USAGERID_OFFSET;
    }

    private UsagerBean convertUsagerCourrierDTOToUsagerBean(UsagerCourrierDTO uc) {
        UsagerBean ub = new UsagerBean();
        ub.setAdresse1(uc.getAdresse1());
        ub.setAdresse2(uc.getAdresse2());
        ub.setCodePostal(uc.getCodePostal());
        ub.setComplementAdresse(uc.getAdresseComplement());
        ub.setDateCreation(uc.getDateCreation());
        ub.setEmail(uc.getEmail());
        ub.setId(uc.getPkUsagersCourrier());
        ub.setLogin(uc.getLogin());
        ub.setNom(uc.getNom());
        ub.setPrenom(uc.getPrenom());
        ub.setNomPays(uc.getPays());
        ub.setRaisonSociale(uc.getRaisonSociale());
        ub.setTitre(uc.getTitre().shortValue());
        ub.setVille(uc.getVille());

        return ub;
    }

}
