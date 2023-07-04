package mc.gouv.xaf.back.service;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * Interface à implémenter par la démarche, lui permettant de fournir
 * l'historique consécutif à un événement.
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

    public DemandeHistoriqueDTO statusChange(Integer demandeId, String targetState, String customContextParam,
			 Integer usagerId, String agentId);
    
    public DemandeHistoriqueDTO prendreEnCharge(Integer demandeId, String targetState, String agentId);
    
    public DemandeHistoriqueDTO creationDemande(Integer demandeId, Integer usagerId, String agentId);
    
    public DemandeHistoriqueDTO reponseDemandeCompl(Integer demandeId, String targetState, Integer usagerId,
			String agentId);
    
    public DemandeHistoriqueDTO desinscriptionUsager(DemandeDTO demande, Integer usagerId, boolean avecAnnulation);
    
    public DemandeHistoriqueDTO associationDemandeCourrier(DemandeDTO demande, Integer usagerId);
    
    public DemandeHistoriqueDTO traiterFinal(Integer demandeId, String targetState, String agentId);
    
    public DemandeHistoriqueDTO historiqueDuplicationNouvelleDemande(Integer demandeId, Integer oldDemandeId, String demarcheId, String agentId);
    
    public DemandeHistoriqueDTO historiqueDuplicationAncienneDemande(Integer demandeId, Integer oldDemandeId, String demarcheId, String agentId);
    
    public DemandeHistoriqueDTO historiqueDemanderAvisClub(Integer demandeId, String targetState, String agentId);
    
    public DemandeHistoriqueDTO demanderRectification(Integer demandeId, String agentId);
    
    public DemandeHistoriqueDTO updateDemande(DemandeDTO demande, Integer usagerId, String agentId, String targetStateStr);
    
    public DemandeHistoriqueDTO historiqueValidationHierachique(Integer demandeId, String targetState, String agentId);

}
