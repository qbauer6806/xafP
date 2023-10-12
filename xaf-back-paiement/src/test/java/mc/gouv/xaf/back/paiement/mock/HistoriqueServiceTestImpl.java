package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.service.AfHistoService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
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

	@Override
	public DemandeHistoriqueDTO statusChange(Integer demandeId, String targetState, String customContextParam,
			Integer usagerId, String agentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DemandeHistoriqueDTO prendreEnCharge(Integer demandeId, String targetState, String agentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DemandeHistoriqueDTO creationDemande(Integer demandeId, Integer usagerId, String agentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DemandeHistoriqueDTO reponseDemandeCompl(Integer demandeId, String targetState, Integer usagerId,
			String agentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DemandeHistoriqueDTO desinscriptionUsager(DemandeDTO demande, Integer usagerId, boolean avecAnnulation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DemandeHistoriqueDTO associationDemandeCourrier(DemandeDTO demande, Integer usagerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DemandeHistoriqueDTO historiqueDuplicationNouvelleDemande(Integer demandeId, Integer oldDemandeId,
			String demarcheId, String agentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DemandeHistoriqueDTO historiqueDuplicationAncienneDemande(Integer demandeId, Integer oldDemandeId,
			String demarcheId, String agentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DemandeHistoriqueDTO demanderRectification(Integer demandeId, String agentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DemandeHistoriqueDTO updateDemande(DemandeDTO demande, Integer usagerId, String agentId,
			String targetStateStr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DemandeHistoriqueDTO historiqueValidationHierachique(Integer demandeId, String targetState, String agentId) {
		// TODO Auto-generated method stub
		return null;
	}
}
