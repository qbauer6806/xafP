package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * Service permettant la manipulation de l'historique des demandes.
 * 
 * @author qdeme
 *
 */
public interface DemandesHistoriqueService {

    /**
     * Permet de récupérer tout l'historique d'une demande
     * @param demande
     * @return
     */
    public List<DemandeHistoriqueDTO> getHistorique(String demarcheId, Integer demandeId);
    
    /**
     * Permet de rajouter une ligne à l'historique d'une demande
     * @param demandeBo
     * @param demandeHistoriqueDto
     * @return
     */
    public DemandeHistoriqueDTO saveHistorique(String demarcheId, Integer demandeId, DemandeHistoriqueDTO demandeHistoriqueDto);
    
}
