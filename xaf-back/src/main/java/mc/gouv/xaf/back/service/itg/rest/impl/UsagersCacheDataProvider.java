package mc.gouv.xaf.back.service.itg.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.UsagersCourrierService;
import mc.gouv.xaf.back.service.itg.gichuni.api.GichuniApiClient;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.back.service.utils.UsagersUtils;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import mc.gouv.xaf.caching.GouvCacheDataProvider;

/**
 * 
 * DataProvider du cache des usagers courrier
 * 
 * @author qdeme
 *
 */
@Profile("gouv")
@Component
public class UsagersCacheDataProvider implements GouvCacheDataProvider<Integer, GichuniUsagerDTO> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersCacheDataProvider.class);

    @Autowired
    private GichuniApiClient gichuniApiClient;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private UsagersCourrierService usagersCourrierService;
    
    @Autowired
    private AccessService accessService;

    /**
     * Filtre les ids des usagers en fonction de s'ils sont en ligne ou non.
     */
    private void filtrerUsagersIds(List<Integer> usagersCourriersIds, List<Integer> usagersInternetIds) {
        List<Integer> usagersIds = accessService.getUsagersIds();
        for (Integer usagerId : usagersIds) {
            if (!isUsagerCourrier(usagerId)) {
                usagersInternetIds.add(usagerId);

            } else {
                usagersCourriersIds.add(usagerId);
            }
        }
    }

    /**
     * Construit les requettes paginées à envoyer au client gichuni
     */
    private List<List<Integer>> pagesBuilder(List<Integer> usagersInternetIds) {
        // Paginer par pages de (500 par défaut)
        Integer pageSize = gouvPropertiesResolver.getUsagersPageSize();
        LOGGER.info("Pagination : appel par pages de {}... {} usagers à récupérer...", pageSize, usagersInternetIds.size());
        List<List<Integer>> pages = new ArrayList<>();
        pages.add(new ArrayList<>());
        int pageCounter = 0;
        for (Integer usager : usagersInternetIds) {
            if (pages.get(pageCounter).size() == pageSize) {
                pages.add(new ArrayList<>());
                pageCounter++;
            }
            pages.get(pageCounter).add(usager);
        }
        LOGGER.info("Résultat de la pagination : {} pages", pages.size());
        return pages;
    }

    /**
     * Récupère les usagers en ligne
     */
    private void getUsagersInternet(List<Integer> usagersInternetIds, List<GichuniUsagerDTO> usagers) {
        // Si des usagers se sont désinscrits, il m'en sortira moins que le nombre d'ids donnés en paramètre
        if (!usagersInternetIds.isEmpty()) {
            LOGGER.info("Récupération des usagers INTERNET: {}", usagersInternetIds);
            if (usagersInternetIds.size() == 1) {
                GichuniUsagerDTO usagerBean = gichuniApiClient.getUsager(usagersInternetIds.get(0));
                if (usagerBean != null) {
                    usagers.add(usagerBean);
                }
            } else {
                List<List<Integer>> pages = pagesBuilder(usagersInternetIds);
                usagers = new ArrayList<>();
                int nb = 0;
                for (List<Integer> page : pages) {
                    nb++;
                    LOGGER.info("Appel pour la page {} : {}", nb, page);
                    List<GichuniUsagerDTO> usagersTmp = gichuniApiClient.getUsagers(page);
                    if (usagersTmp != null) {
                        usagers.addAll(usagersTmp);
                    }
                }

                LOGGER.info("Fin appel paginé : {}  usagers récupérés.", usagers.size());
            }
        }
    }
    
    @Override
    public ConcurrentHashMap<Integer, GichuniUsagerDTO> getAll() {
        List<GichuniUsagerDTO> usagers = new ArrayList<>();
        List<Integer> usagersCourriersIds = new ArrayList<>();
        List<Integer> usagersInternetIds = new ArrayList<>();

        // Récupération de tous les ids des usagers
        filtrerUsagersIds(usagersCourriersIds, usagersInternetIds);

        // Récupération des usagers Internet
        getUsagersInternet(usagersInternetIds, usagers);

        LOGGER.info("Récupération des usagers COURRIER: {}", usagersCourriersIds);
        List<GichuniUsagerDTO> usagersCourriers = new ArrayList<>();
        //Voir pour faire la fonction qui prend une liste d'ids
        for (Integer usagerCourrierId : usagersCourriersIds) {
            UsagerCourrierDTO uc = usagersCourrierService.getUsagerCourrier(usagerCourrierId);
            if (uc != null) {
            	GichuniUsagerDTO ub = UsagersUtils.convertUsagerCourrierDTOToGichuniUsagerDTO(uc);
                usagersCourriers.add(ub);
            }
        }
        // Ajout des usagers courriers à la liste
        usagers.addAll(usagersCourriers);
        LOGGER.info("Liste des usagers : {}", usagers);
        
        // Transformation de la liste vers la ConcurrentHashMap
        ConcurrentHashMap<Integer, GichuniUsagerDTO> usagersMap = new ConcurrentHashMap<>();
        for (GichuniUsagerDTO usager : usagers) {
            usagersMap.put(usager.getId(), usager);
        }
        return usagersMap;
    }

    @Override
    public GichuniUsagerDTO get(Integer key) {
        if (isUsagerCourrier(key)) {
            UsagerCourrierDTO uc = usagersCourrierService.getUsagerCourrier(key);
            return UsagersUtils.convertUsagerCourrierDTOToGichuniUsagerDTO(uc);
        } else {
            return gichuniApiClient.getUsager(key);
        }
    }
    
    public static boolean isUsagerCourrier(Integer usagerId) {
        return usagerId > DemarchesUtils.USAGERID_OFFSET;
    }

}
