package mc.gouv.xaf.back.service;

import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * Interface à implémenter par la démarche, lui permettant de fournir
 * l'historique consécutif à un événement.
 *
 * @author mboutelier.ext
 */
public interface AfHistoService {

    DemandeHistoriqueDTO traiterFinal(Integer demandeId, String targetState, String agentId);

}
