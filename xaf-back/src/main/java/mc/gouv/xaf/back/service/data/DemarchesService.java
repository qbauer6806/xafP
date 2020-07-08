package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.back.data.entity.DemarchesBO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;

/**
 * Service permettant la manipulation des démarches.
 * 
 * @author qdeme
 *
 */
public interface DemarchesService {

    /**
     * Permet de récupérer la démarche correspondant au DemarcheID
     * @param démarche
     * @return La démarche demandée
     */
    public DemarcheDTO getDemarche(String demarcheId);
    
    /**
     * Permet de récupérer la démarche correspondant au DemarcheID
     * Fonction réservée aux autres Services
     * @param demarche
     * @return
     */
    public DemarchesBO getCheckDemarche(String demarcheId);
    
    /**
     * Permet de modifier une démarche à partir du DemarcheID
     * @param démarche
     * @return La démarche modifiée
     */
    public DemarcheDTO updateDemarche(DemarcheDTO demarche);
    
}
