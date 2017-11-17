package mc.gouv.af.back.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.service.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.AccessService;
import mc.gouv.dem.service.UsagersCourrierService;
import mc.gouv.dem.service.exception.DemarchesServiceException;
import mc.gouv.dem.shared.model.UsagerCourrierDTO;
import mc.gouv.servicerest.usager.ReferentielUsagersClient;
import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.xboot.caching.GouvCacheDataProvider;

/**
 * 
 * DataProvider du cache des usagers courrier
 * 
 * @author qdeme
 *
 */
@Profile("gouv")
@Component
public class UsagersCacheDataProvider implements GouvCacheDataProvider<Integer, UsagerBean> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersCacheDataProvider.class);

    @Autowired
    private ReferentielUsagersClient referentielUsagersClient;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private UsagersCourrierService usagersCourrierService;
    
    @Autowired
    private AccessService accessService;
    
    public static final int USAGERID_OFFSET = 1000000000;
    
    @Override
    public ConcurrentHashMap<Integer, UsagerBean> getAll() {
        List<UsagerBean> usagers = new ArrayList<UsagerBean>();

        //Récupération de tous les ids des usagers
        //List<Integer> usagersIds = demClient.getUsagersIds(gouvPropertiesResolver.getDemarcheId());
        List<Integer> usagersIds = accessService.getUsagersIds(gouvPropertiesResolver.getDemarcheId());
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
//                UsagerCourrierDTO uc = demClient.getUsagerCourrier(gouvPropertiesResolver.getDemarcheId(),
//                        usagerCourrierId);
            UsagerCourrierDTO uc = null;
            try {
                uc = usagersCourrierService.getUsagerCourrier(gouvPropertiesResolver.getDemarcheId(), usagerCourrierId);
                UsagerBean ub = convertUsagerCourrierDTOToUsagerBean(uc);
                usagersCourriers.add(ub);
            }
            catch (DemarchesServiceException dse) {
                // Usager courrier introuvable
            }

        }
        //Ajout des usagers courriers à la liste
        usagers.addAll(usagersCourriers);

        LOGGER.info(usagers.toString());
        
        // Transformation de la liste vers la ConcurrentHashMap
        ConcurrentHashMap<Integer, UsagerBean> usagersMap = new ConcurrentHashMap<Integer, UsagerBean>();
        for (UsagerBean usager : usagers) {
            usagersMap.put(usager.getId(), usager);
        }
        return usagersMap;
    }

    @Override
    public UsagerBean get(Integer key) {
        if (isUsagerCourrier(key)) {
            UsagerCourrierDTO uc = usagersCourrierService.getUsagerCourrier(gouvPropertiesResolver.getDemarcheId(), key);
            UsagerBean ub = convertUsagerCourrierDTOToUsagerBean(uc);
            return ub;
        }
        else {
            UsagerBean usagerBean = referentielUsagersClient.getUsager(key);
            return usagerBean;
        }
    }
    
    private UsagerBean convertUsagerCourrierDTOToUsagerBean(UsagerCourrierDTO uc) {
        if (uc == null) {
            return null;
        }
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

        if (uc.getTitre() != null) {
            ub.setTitre(uc.getTitre().shortValue());
        }

        ub.setVille(uc.getVille());

        return ub;
    }
    
    public static boolean isUsagerCourrier(Integer usagerId) {
        return usagerId > USAGERID_OFFSET;
    }

}
