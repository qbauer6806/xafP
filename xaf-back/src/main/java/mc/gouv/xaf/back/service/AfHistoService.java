package mc.gouv.xaf.back.service;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * Interface à implémenter par la démarche, lui permettant de fournir l'historique consécutif à un événement.
 *
 * @author mboutelier.ext
 */
public interface AfHistoService {

    String SYSTEME = "Système";

    /**
     * Ajoute une ligne à l'historique d'une demande après une action automatique réalisée par le système.
     */
    DemandeHistoriqueDTO actionSysteme(Integer demandeId, String targetState, String action);

    /**
     * Ajoute une ligne à l'historique d'une demande après une action réalisée par un usager FO.
     */
    DemandeHistoriqueDTO actionUsager(Integer demandeId, Integer usagerId, String targetState, String action);

    DemandeHistoriqueDTO statusChange(Integer demandeId, String targetState, String customContextParam,
            Integer usagerId, String agentId);

    default DemandeHistoriqueDTO statusChange(Integer demandeId, String targetState, String customContextParam,
            Integer usagerId, String agentId, boolean isModif) {
        return null;
    }

    default DemandeHistoriqueDTO statusChange(Integer demandeId, String targetState, String customContextParam,
            Integer usagerId, String agentId, String codeMotif) {
        return null;
    }

    default DemandeHistoriqueDTO statusChange(Integer demandeId, String targetState, String customContextParam,
            Integer usagerId, String agentId, boolean isModif, String codeMotif) {
        return null;
    }

    DemandeHistoriqueDTO prendreEnCharge(Integer demandeId, String targetState, String agentId);

    DemandeHistoriqueDTO creationDemande(Integer demandeId, Integer usagerId, String agentId);

    DemandeHistoriqueDTO reponseDemandeCompl(Integer demandeId, String targetState, Integer usagerId, String agentId,
            String affecteId);

    DemandeHistoriqueDTO desinscriptionUsager(DemandeDTO demande, Integer usagerId, boolean avecAnnulation);

    DemandeHistoriqueDTO associationDemandeCourrier(DemandeDTO demande, Integer usagerId);

    DemandeHistoriqueDTO traiterFinal(Integer demandeId, String targetState, String agentId);

    DemandeHistoriqueDTO historiqueDuplicationNouvelleDemande(Integer demandeId, Integer oldDemandeId, String agentId);

    DemandeHistoriqueDTO historiqueDuplicationAncienneDemande(Integer demandeId, Integer oldDemandeId, String agentId);

    default DemandeHistoriqueDTO demanderRectification(Integer demandeId, String agentId) {
        return null;
    }

    default DemandeHistoriqueDTO historiqueValidationHierachique(Integer demandeId, String targetState,
            String agentId) {
        return null;
    }

    default DemandeHistoriqueDTO reprendreEnCharge(Integer demandeId, String statutName, String agentId) {
        return null;
    }

    void saveHisto(Integer demandeId, DemandeHistoriqueDTO histo);

    default DemandeHistoriqueDTO updateDemande(DemandeDTO demandeDto, Integer usagerId, String agentId,
            String targetState) {
        return null;
    }
}
