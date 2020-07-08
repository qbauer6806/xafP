#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * 
 * Interface à implémenter par la démarche, lui permettant de fournir
 * l'historique consécutif à un événement.
 * 
 * @author mpavone
 *
 */
public interface HistoService {

	public DemandeHistoriqueDTO statusChange(Integer demandeId, String targetState, String customContextParam,
			Integer usagerId, String agentId);

	public DemandeHistoriqueDTO prendreEnCharge(Integer demandeId, String targetState, String agentId);

	public DemandeHistoriqueDTO reponseDemandeCompl(Integer demandeId, String targetState, Integer usagerId,
			String agentId);

	public DemandeHistoriqueDTO creationDemande(Integer demandeId, Integer usagerId, String agentId);

	public DemandeHistoriqueDTO desinscriptionUsager(DemandeDTO demande, Integer usagerId, boolean avecAnnulation);

	public DemandeHistoriqueDTO associationDemandeCourrier(DemandeDTO demande, Integer usagerId);

	public DemandeHistoriqueDTO traiterFinal(Integer demandeId, String targetState, String agentId);

	public DemandeHistoriqueDTO historiqueDuplicationNouvelleDemande(Integer demandeId, Integer oldDemandeId, String demarcheId, String agentId);

	public DemandeHistoriqueDTO historiqueDuplicationAncienneDemande(Integer demandeId, Integer oldDemandeId, String demarcheId, String agentId);

}
