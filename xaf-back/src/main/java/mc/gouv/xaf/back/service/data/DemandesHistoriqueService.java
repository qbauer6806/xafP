package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

import java.util.List;

/**
 * Service permettant la manipulation de l'historique des demandes.
 *
 * @author qdeme
 */
public interface DemandesHistoriqueService {

    /**
     * Permet de récupérer tout l'historique d'une demande
     */
    List<DemandeHistoriqueDTO> getHistorique(Integer demandeId);

    /**
     * Permet de rajouter une ligne à l'historique d'une demande
     */
    DemandeHistoriqueDTO saveHistorique(Integer demandeId, DemandeHistoriqueDTO demandeHistoriqueDto);

    /**
     * Permet de rajouter une ligne à l'historique d'une demande
     * pour les actions automatiques (userId et agentId sont null)
     */
    DemandeHistoriqueDTO saveHistoriqueActionAuto(Integer demandeId, DemandeHistoriqueDTO demandeHistoriqueDto);


}
