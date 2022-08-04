package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.service.AfHistoService;

/**
 * Extenstion de l'interface AfHistoService pour les historiques de la demande spécifiques au paiement
 * @author mboutelier.ext
 */
public interface PaiementDemandeHistoriqueService extends AfHistoService {

    String SYSTEME = "Système";

    /**
     * Ajoute une ligne à l'historique d'une demande après une action automatique réalisée par le système.
     */
    void actionSysteme(Integer demandeId, String targetState, String action);

    void paiementEnLigne(Integer demandeId, Integer usagerId);

}
