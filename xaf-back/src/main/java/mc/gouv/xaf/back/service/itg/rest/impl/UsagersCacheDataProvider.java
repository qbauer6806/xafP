package mc.gouv.xaf.back.service.itg.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.UsagersCourrierService;
import mc.gouv.xaf.back.service.itg.gichuni.api.GichuniApiClient;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.back.service.utils.UsagersUtils;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
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
    
    @Override
    public ConcurrentHashMap<Integer, GichuniUsagerDTO> getAll() {
        List<GichuniUsagerDTO> usagers = new ArrayList<GichuniUsagerDTO>();

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
        //Si des usagers se sont désinscrits, il m'en sortira moins que le nombre d'ids donnés en paramètre
        if (!usagersInternetIds.isEmpty()) {
        	LOGGER.info("Récupération des usagers INTERNET: {}", usagersInternetIds);
            if (usagersInternetIds.size() == 1) {
                GichuniUsagerDTO usagerBean = gichuniApiClient.getUsager(usagersInternetIds.get(0));
                if (usagerBean != null) {
                    usagers.add(usagerBean);
                }

            } else {
            	
            	// Paginer par pages de (500 par défaut)
            	Integer pageSize = gouvPropertiesResolver.getUsagersPageSize();
            	LOGGER.info("Pagination : appel par pages de " + pageSize + "... " + usagersInternetIds.size() + " usagers à récupérer");
            	List<List<Integer>> pages = new ArrayList<List<Integer>>();
            	pages.add(new ArrayList<Integer>());
            	int pageCounter = 0;
            	for (Integer usager : usagersInternetIds) {
            		if (pages.get(pageCounter).size() == pageSize) {
            			pages.add(new ArrayList<Integer>());
            			pageCounter++;
            		}
            		
            		pages.get(pageCounter).add(usager);
            	}
            	
            	LOGGER.info("Résultat de la pagination : " + pages.size() + " pages");
            	
            	usagers = new ArrayList<GichuniUsagerDTO>();
            	int nb = 0;
            	for (List<Integer> page : pages) {
            		nb++;
            		List<GichuniUsagerDTO> usagersTmp = new ArrayList<GichuniUsagerDTO>();
            		LOGGER.info("Appel pour la page " + nb + " : " + page);
            		usagersTmp = gichuniApiClient.getUsagers(page);
            		if (usagersTmp != null) {
            			usagers.addAll(usagersTmp);
            		}
            	}
            	
            	LOGGER.info("Fin appel paginé : " + usagers.size() + " usagers récupérés");
            }
        }

        LOGGER.info("Récupération des usagers COURRIER: {}", usagersCourriersIds);
        List<GichuniUsagerDTO> usagersCourriers = new ArrayList<GichuniUsagerDTO>();
        //Voir pour faire la fonction qui prend une liste d'ids
        for (Integer usagerCourrierId : usagersCourriersIds) {
//                UsagerCourrierDTO uc = demClient.getUsagerCourrier(gouvPropertiesResolver.getDemarcheId(),
//                        usagerCourrierId);
            UsagerCourrierDTO uc = usagersCourrierService.getUsagerCourrier(gouvPropertiesResolver.getDemarcheId(), usagerCourrierId);
            if (uc != null) {
            	GichuniUsagerDTO ub = UsagersUtils.convertUsagerCourrierDTOToGichuniUsagerDTO(uc);
                usagersCourriers.add(ub);
            }

        }
        //Ajout des usagers courriers à la liste
        usagers.addAll(usagersCourriers);

        LOGGER.info(usagers.toString());
        
        // Transformation de la liste vers la ConcurrentHashMap
        ConcurrentHashMap<Integer, GichuniUsagerDTO> usagersMap = new ConcurrentHashMap<Integer, GichuniUsagerDTO>();
        for (GichuniUsagerDTO usager : usagers) {
            usagersMap.put(usager.getId(), usager);
        }
        return usagersMap;
    }

    @Override
    public GichuniUsagerDTO get(Integer key) {
        if (isUsagerCourrier(key)) {
            UsagerCourrierDTO uc = usagersCourrierService.getUsagerCourrier(gouvPropertiesResolver.getDemarcheId(), key);
            GichuniUsagerDTO ub = UsagersUtils.convertUsagerCourrierDTOToGichuniUsagerDTO(uc);
            return ub;
        }
        else {
        	GichuniUsagerDTO usagerBean = gichuniApiClient.getUsager(key);
            return usagerBean;
        }
    }
    
    public static boolean isUsagerCourrier(Integer usagerId) {
        return usagerId > DemarchesUtils.USAGERID_OFFSET;
    }

}
