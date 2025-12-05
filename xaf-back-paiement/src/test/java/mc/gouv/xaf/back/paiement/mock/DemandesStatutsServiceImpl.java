package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemandesStatutsServiceImpl implements DemandesStatutsService {

    @Override
    public DemandeDTO updateStatut(Integer demandeId, String statutName, String agentId, Integer usagerId,
            String codeMotif, String commentaire, String texteAEnvoyer) {
        return null;
    }

    @Override
    public DemandeDTO updateStatut(DemandeBO demande, String statutName, String agentId, Integer usagerId,
            String codeMotif, String commentaire, String texteAEnvoyer) {
        return null;
    }

    @Override
    public void updateMultipleStatuts(List<DemandeBO> demandes, String statutName) {
    }

    @Override
    public DemandeStatutDTO getStatut(Integer demandeId) {
        return null;
    }

    @Override
    public List<DemandeStatutDTO> getStatuts(Integer demandeId) {
        return List.of();
    }

}
