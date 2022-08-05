package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.service.PaiementDemandeHistoriqueService;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import org.springframework.stereotype.Component;

@Component
public class HistoriqueServiceTestImpl implements PaiementDemandeHistoriqueService {
    @Override
    public DemandeHistoriqueDTO actionSysteme(Integer demandeId, String targetState, String action) {
        return null;
    }

    @Override
    public DemandeHistoriqueDTO paiementEnLigne(Integer demandeId, Integer usagerId) {
        return null;
    }

    @Override
    public DemandeHistoriqueDTO traiterFinal(Integer demandeId, String targetState, String agentId) {
        return null;
    }
}
