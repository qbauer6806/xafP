#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service;

import mc.gouv.xaf.back.service.AfHistoService;
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
public interface HistoService extends AfHistoService {

	DemandeHistoriqueDTO statusChange(Integer demandeId, String targetState, String customContextParam,
		Integer usagerId, String agentId);

	DemandeHistoriqueDTO prendreEnCharge(Integer demandeId, String targetState, String agentId);

	DemandeHistoriqueDTO reponseDemandeCompl(Integer demandeId, String targetState, Integer usagerId,
		String agentId);

	DemandeHistoriqueDTO creationDemande(Integer demandeId, Integer usagerId, String agentId);

	DemandeHistoriqueDTO desinscriptionUsager(DemandeDTO demande, Integer usagerId, boolean avecAnnulation);

	DemandeHistoriqueDTO associationDemandeCourrier(DemandeDTO demande, Integer usagerId);

	DemandeHistoriqueDTO historiqueDuplicationNouvelleDemande(Integer demandeId, Integer oldDemandeId, String demarcheId, String agentId);

	DemandeHistoriqueDTO historiqueDuplicationAncienneDemande(Integer demandeId, Integer oldDemandeId, String demarcheId, String agentId);

}
