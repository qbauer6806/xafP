package mc.gouv.af.back.util;

import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeHistoriqueDTO;

/**
 * 
 * @author qdeme
 *
 */
public interface HistoService {

    public DemandeHistoriqueDTO statusChange(Integer demandeId, String targetState,
            String customContextParam, Integer usagerId, String agentId);

    public DemandeHistoriqueDTO prendreEnCharge(Integer demandeId, String targetState, String agentId);

    public DemandeHistoriqueDTO reponseDemandeCompl(Integer demandeId, String targetState, Integer usagerId,
            String agentId);

    public DemandeHistoriqueDTO traiter(Integer demandeId, String targetState, String agentId);

    public DemandeHistoriqueDTO creationDemande(Integer demandeId, Integer usagerId, String agentId);

    public DemandeHistoriqueDTO desinscriptionUsager(DemandeDTO demande, Integer usagerId, boolean avecAnnulation);

    public DemandeHistoriqueDTO associationDemandeCourrier(DemandeDTO demande, Integer usagerId);

}
