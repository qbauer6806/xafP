package mc.gouv.af.back.util;

import mc.gouv.dem.apishared.model.DemandeHistoriqueDTO;
import mc.gouv.dem.apishared.model.DemandeStatutEnum;

/**
 * 
 * @author qdeme
 *
 */
public interface HistoService {

    public DemandeHistoriqueDTO statusChange(Integer demandeId, DemandeStatutEnum targetState,
            String customContextParam, Integer usagerId, String agentId);

    public DemandeHistoriqueDTO prendreEnCharge(Integer demandeId, DemandeStatutEnum targetState, String agentId);

    public DemandeHistoriqueDTO reponseDemandeCompl(Integer demandeId, DemandeStatutEnum targetState, Integer usagerId,
            String agentId);

    public DemandeHistoriqueDTO traiter(Integer demandeId, DemandeStatutEnum targetState, String agentId);

    DemandeHistoriqueDTO creationDemande(Integer demandeId, Integer usagerId, String agentId);

}
