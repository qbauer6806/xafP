package mc.gouv.xaf.back.service;

import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * Interface à implémenter par la démarche, lui permettant de fournir
 * l'historique consécutif à un événement.
 *
 * @author mpavone
 */
public abstract class HistoService implements AfHistoService {

	@Override
	public DemandeHistoriqueDTO actionSysteme(Integer demandeId, String targetState, String action) {
		return null;
	}

	@Override
	public DemandeHistoriqueDTO actionUsager(Integer demandeId, Integer usagerId, String targetState, String action) {
		return null;
	}

}
