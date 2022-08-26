package mc.gouv.xaf.back.service;

import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * Interface à implémenter par la démarche, lui permettant de fournir
 * l'historique consécutif à un événement.
 *
 * @author mboutelier.ext
 */
public interface AfHistoService {

    String SYSTEME = "Système";

    DemandeHistoriqueDTO traiterFinal(Integer demandeId, String targetState, String agentId);

    /**
     * Ajoute une ligne à l'historique d'une demande après une action automatique réalisée par le système.
     */
    DemandeHistoriqueDTO actionSysteme(Integer demandeId, String targetState, String action);

}
