package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.service.AfHistoService;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import org.springframework.stereotype.Component;

@Component
public class HistoriqueServiceTestImpl implements AfHistoService {
    @Override
    public DemandeHistoriqueDTO actionSysteme(Integer demandeId, String targetState, String action) {
        return null;
    }

    @Override
    public DemandeHistoriqueDTO actionUsager(Integer demandeId, Integer usagerId, String targetState, String action) {
        return null;
    }

    @Override
    public DemandeHistoriqueDTO traiterFinal(Integer demandeId, String targetState, String agentId) {
        return null;
    }
}
