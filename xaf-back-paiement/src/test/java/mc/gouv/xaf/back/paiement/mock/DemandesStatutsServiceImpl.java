package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemandesStatutsServiceImpl implements DemandesStatutsService {
    @Override
    public DemandeDTO updateStatut(Integer demandeId, StatutPublicOuInterneDTO statut, String agentId, Integer usagerId, String codeMotif, String commentaire, String texteAEnvoyer) {
        return null;
    }

    @Override
    public DemandeDTO updateStatut(Integer demandeId, DemandeStatutDTO statut, String agentId, Integer usagerId, String codeMotif, String commentaire, String texteAEnvoyer) {
        return null;
    }

    @Override
    public DemandeDTO updateStatut(DemandeBO demande, StatutPublicOuInterneDTO statut, String agentId, Integer usagerId, String codeMotif, String commentaire, String texteAEnvoyer) {
        return null;
    }

    @Override
    public List<DemandeDTO> updateMultipleStatuts(List<DemandeDTO> demandes, StatutPublicOuInterneDTO statut) {
        return null;
    }

    @Override
    public DemandeStatutDTO getStatut(Integer demandeId) {
        return null;
    }

    @Override
    public List<DemandeStatutDTO> getStatuts(Integer demandeId) {
        return null;
    }

    @Override
    public void clonerStatuts(DemandeBO demandeBo, DemandeBO newDemandeBo) {

    }
}
