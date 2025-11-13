package mc.gouv.xaf.back.service.data;

import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;

/**
 * Service permettant la manipulation des demandes.
 *
 * @author qdeme
 */
public interface DemandesStatutsService {

    /**
     * Permet d'ajouter un statut à une demande
     *
     * @param demandeId
     *         ID de la demande
     * @param statutName
     *         Le nouveau statut
     * @param agentId
     *         AgentID à associer au statut
     * @param usagerId
     *         UsagerID à associer au statut
     * @param codeMotif
     *         Le codeMotif du motif associé au changement de statut, si nécessaire
     * @param commentaire
     *         Le commentaire associé au changement de statut, si nécessaire
     * @param texteAEnvoyer
     *         Le texte du justificatif / courrier à envoyer à l'usagé associé au changement de statut, si nécessaire
     * @return L'objet DTO de la demande mise à jour
     */
    DemandeDTO updateStatut(Integer demandeId, String statutName, String agentId, Integer usagerId, String codeMotif,
            String commentaire, String texteAEnvoyer);

    /**
     * Permet d'ajouter un statut à une demande, version appelable par d'autres services, sans check préalable
     *
     * @param demande
     *         La demande
     * @param statutName
     *         Le nouveau statut
     * @param agentId
     *         AgentID à associer au statut
     * @param usagerId
     *         UsagerID à associer au statut
     * @param codeMotif
     *         Le codeMotif du motif associé au changement de statut, si nécessaire
     * @param commentaire
     *         Le commentaire associé au changement de statut, si nécessaire
     * @param texteAEnvoyer
     *         Le texte du justificatif / courrier à envoyer à l'usagé associé au changement de statut, si nécessaire
     * @return L'objet BO de la demande mise à jour
     */
    DemandeDTO updateStatut(DemandeBO demande, String statutName, String agentId, Integer usagerId, String codeMotif,
            String commentaire, String texteAEnvoyer);

    /**
     * Permet de mettre le même statut sur plusieurs demandes en même temps.
     *
     * @param demandes
     *         La liste des demandes à mettre à jour
     * @param statutName
     *         Le nouveau statut
     * @return La liste à jour des demandes
     */
    void updateMultipleStatuts(List<DemandeBO> demandes, String statutName);

    /**
     * Récupérer tous les statuts d'une demande
     *
     * @param demandeId
     *         ID de la demande
     * @return Une liste de statut DTO
     */
    List<DemandeStatutDTO> getStatuts(Integer demandeId);

}
