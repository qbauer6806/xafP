package mc.gouv.xaf.back.service.purge;

import java.util.Date;
import java.util.List;

import mc.gouv.xaf.shared.dto.PurgeDemandeDTO;

public interface PurgeDemandesService {

	void purgerDemandesDansStatuts(List<String> statuts, int jours) throws Exception;

	/**
	 * Récupère toutes les demandes ayant comme statut "SUPPRIMEE"
	 * @return statistiques
	 */
	List<PurgeDemandeDTO> getDemandesPurgees();
	
	
	/**
	 * Spécifie la méthode d'envoi des emails aux agents
	 * @param demandesAPurger
	 * @param delai
	 */
	void envoisMailAgentPurge(String demandesAPurger, String delai);

	/**
	 * Récupère la dernière execution du job de purge
	 */
	Date getDateDerniereExecution();
	
}
