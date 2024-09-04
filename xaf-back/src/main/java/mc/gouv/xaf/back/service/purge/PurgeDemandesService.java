package mc.gouv.xaf.back.service.purge;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Date;
import java.util.List;

public interface PurgeDemandesService {

	String DEMANDES_TRIGGER_NAME = "PurgeDemandesSchedulingTrigger";
	String PAIEMENTS_TRIGGER_NAME = "PurgeDemandesPaiementsSchedulingTrigger";

	void purgerDemandesDansStatuts(List<String> statuts, int jours) throws JsonProcessingException;

	/**
	 * Récupère toutes les demandes ayant comme statut "SUPPRIMEE"
	 * @return statistiques
	 */
    List<Object> getDemandesPurgees();
	
	/**
	 * Spécifie la méthode d'envoi des emails aux agents
	 */
	void envoisMailAgentPurge(String demandesAPurger, String delai);

	/**
	 * Récupère la dernière execution du job de purge
	 */
	Date getDateDerniereExecution();
	
}
