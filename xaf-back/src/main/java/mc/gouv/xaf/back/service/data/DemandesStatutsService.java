package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;

/**
 * Service permettant la manipulation des demandes.
 * 
 * @author qdeme
 *
 */
public interface DemandesStatutsService {

    /**
     * Permet d'ajouter un statut à une demande
     * @param demarcheId ID de la démarche
     * @param demandeId ID de la demande
     * @param statut
     * @param agentId AgentID à associer au statut
     * @param usagerId UsagerID à associer au statut
     * @param codeMotif Le codeMotif du motif associé au changement de statut, si nécessaire
     * @param commentaire Le commentaire associé au changement de statut, si nécessaire
     * @param texteAEnvoyer Le texte du justificatif / courrier à envoyer à l'usagé associé au changement de statut, si nécessaire
     * @return La demande mise à jour
     */
    public DemandeDTO updateStatut(String demarcheId, Integer demandeId, String statut, String agentId, Integer usagerId, String codeMotif, String commentaire, String texteAEnvoyer);
    
    /**
     * Permet d'ajouter un statut à une demande, version appelable par d'autres services, sans check préalable
     * @param demande
     * @param statut
     * @param agentId AgentID à associer au statut
     * @param usagerId UsagerID à associer au statut
     * @param codeMotif Le codeMotif du motif associé au changement de statut, si nécessaire
     * @param commentaire Le commentaire associé au changement de statut, si nécessaire
     * @param texteAEnvoyer Le texte du justificatif / courrier à envoyer à l'usagé associé au changement de statut, si nécessaire
     * @return
     */
    public DemandeBO updateStatut(DemandeBO demande, String statut, String agentId, Integer usagerId, String codeMotif, String commentaire, String texteAEnvoyer);
    
    /**
     * Récupérer le dernier statut d'une demande
     * @param demarcheId ID de la démarche
     * @param demandeId ID de la demande
     * @return
     */
    public DemandeStatutDTO getStatut(String demarcheId, Integer demandeId);
    
    /**
     * Récupérer tous les statuts d'une demande
     * @param demarcheId ID de la démarche
     * @param demandeId ID de la demande
     * @return
     */
    public List<DemandeStatutDTO> getStatuts(String demarcheId, Integer demandeId);
    
}
